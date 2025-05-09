package com.example.carmmunity_capstone.service;

import com.example.carmmunity_capstone.entity.*;
import com.example.carmmunity_capstone.dto.*;
import com.example.carmmunity_capstone.exception.CommentNotFoundException;
import com.example.carmmunity_capstone.exception.PostNotFoundException;
import com.example.carmmunity_capstone.exception.UnAuthorizedAccessException;
import com.example.carmmunity_capstone.repository.CommentLikeRepository;
import com.example.carmmunity_capstone.repository.CommentRepository;
import com.example.carmmunity_capstone.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    public CommentService(CommentRepository commentRepository, CommentLikeRepository commentLikeRepository, PostRepository postRepository, UserService userService) {
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.postRepository = postRepository;
        this.userService = userService;
    }


    // 댓글 저장
    @Transactional
    public void save(CommentSaveRequest commentSaveRequest) {
        User user = userService.getAuthenticatedUser();


        // 존재하는 게시글인지 검증
        Integer postId = commentSaveRequest.getPostId();
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다"));

        // 부모 댓글 찾기 (null일 경우 조회하지 않음)
        Comment parentComment = null;
        if (commentSaveRequest.getParentCommentId() != null) {
            parentComment = commentRepository.findById(commentSaveRequest.getParentCommentId())
                    .orElseThrow(() -> new CommentNotFoundException("부모 댓글이 존재하지 않습니다."));
        }

        // 언급한 회원 찾기 (null일 경우 조회하지 않음)
        User mentionedUser = null;
        if (commentSaveRequest.getMentionUserId() != null) {
            mentionedUser = userService.findById(commentSaveRequest.getMentionUserId())
                    .orElse(null);  // 언급한 유저가 없는 건 허용 가능
        }


        // 댓글 저장
        Comment comment = Comment.builder()
                .commentContent(commentSaveRequest.getContent())
                .deletedAt(null)
                .totalLike(0)
                .post(post)
                .user(user)
                .parentComment(parentComment)
                .mentionedUser(mentionedUser)
                .build();
        commentRepository.save(comment);
        System.out.println("comment = " + comment);
    }


    // 게시글 id로 댓글 조회
    @Transactional(readOnly = true)
    public Page<CommentSearchResponse> getComments(Integer parentCommentId, int page, int size, CommentOrderBy orderBy, Sort.Direction direction) {
        Optional<User> user = userService.getUserIfAuthenticated();

        Sort sort = Sort.by(direction, orderBy.getField());
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Comment> comments = commentRepository.findByParentId(parentCommentId, pageable);

        return comments.map(comment -> {
            CommentSearchResponse dto = new CommentSearchResponse();
            dto.setCommentId(comment.getCommentId());
            dto.setCommentContent(comment.getCommentContent());
            dto.setTotalLike(comment.getTotalLike());

            // 작성자 여부 판단
            boolean isAuthor = user
                    .map(u -> u.getUserId().equals(comment.getUser().getUserId()))
                    .orElse(false);
            dto.setAuthor(isAuthor);

            // 언급 유저 정보 포함
//            dto.setMentionedUser(comment.getMentionedUser());

            return dto;
        });
    }



    // 댓글 수정
    @Transactional
    public void update(Integer commentId, CommentUpdateRequest commentUpdateRequest) {
        User user = userService.getAuthenticatedUser();

        // 댓글 id로 댓글 조회
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException("댓글이 존재하지 않습니다."));

        // 작성자 본인인지 확인
        if (!comment.getUser().getUserId().equals(user.getUserId())) {
            throw new UnAuthorizedAccessException("작성자만 댓글을 수정할 수 있습니다.");
        }

        // 댓글 내용 수정 및 저장
        comment.setCommentContent(commentUpdateRequest.getContent());
        commentRepository.save(comment);
    }


    // 댓글 삭제
    @Transactional
    public void delete(Integer commentId) {
        User user = userService.getAuthenticatedUser();

        // 댓글 id로 조회
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException("댓글이 존재하지 않습니다."));

        // 작성자 본인인지 확인
        if (!comment.getUser().getUserId().equals(user.getUserId())) {
            throw new UnAuthorizedAccessException("작성자만 댓글을 수정할 수 있습니다.");
        }

        // 댓글 삭제
        comment.setDeletedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }


    // 댓글 좋아요
    @Transactional
    public CommentLikeResponse likeComment(Integer commentId, CommentLikeRequest request) {
        // JWT 토큰을 통해 인증된 회원 정보 조회
        User user = userService.getAuthenticatedUser();


        // 댓글 찾기
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException("댓글이 존재하지 않습니다.");
        }

        // 댓글 좋아요 있는지 찾기
        CommentLikeId commentLikeId = new CommentLikeId(commentId, user.getUserId());
        CommentLike commentLike = toggleOrCreateLike(commentLikeId);
        commentLikeRepository.save(commentLike);

        // 댓글 테이블에 좋아요 수 반영
        if (commentLike.getLikeStatus()) {
            commentRepository.incrementCommentLike(commentId);
        } else {
            commentRepository.decrementCommentLike(commentId);
        }

        return new CommentLikeResponse(commentLike.getLikeStatus());
    }

    private CommentLike toggleOrCreateLike(CommentLikeId id) {
        return commentLikeRepository.findById(id)
                .map(commentLike -> {
                    commentLike.setLikeStatus(!commentLike.getLikeStatus());
                    return commentLike;
                })
                .orElse(new CommentLike(id, true));
    }
}