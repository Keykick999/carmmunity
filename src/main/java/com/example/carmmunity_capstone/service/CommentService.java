package com.example.carmmunity_capstone.service;

import com.example.carmmunity_capstone.domain.*;
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
    public void save(String authorizationHeader, CommentSaveRequest commentSaveRequest) {
        // JWT 토큰을 통해 인증된 회원 정보 조회
        User user = userService.validateUser(authorizationHeader);


        // 존재하는 게시글인지 검증
        Integer postId = commentSaveRequest.getPostId();
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다"));

        // 부모 댓글 찾기(없으면 null로 설정)
        Comment parentComment = commentRepository.findById(commentSaveRequest.getParentCommentId()).orElse(null);

        // 언급한 회원 찾기(언급 없으면 null로 처리)
        User mentionedUser = userService.findById(commentSaveRequest.getMentionUserId()).orElse(null);


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
    }


    // 게시글 id로 댓글 조회
    @Transactional(readOnly = true)
    public Page<CommentSearchRequest> getComments(String authorizationHeader, Integer parentCommentId, int page, int size, CommentOrderBy orderBy, Sort.Direction direction) {
        Optional<User> user = userService.getUserIfAuthenticated(authorizationHeader);

        Sort sort = Sort.by(direction, orderBy.name().toLowerCase());
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Comment> comments = commentRepository.findByParentId(parentCommentId, pageable);

        return comments.map(comment -> {
            CommentSearchRequest dto = new CommentSearchRequest();
            dto.setCommentId(comment.getCommentId());
            dto.setCommentContent(comment.getCommentContent());
            dto.setTotalLike(comment.getTotalLike());

            // 작성자 여부 판단
            boolean isAuthor = user
                    .map(u -> u.getId().equals(comment.getUser().getId()))
                    .orElse(false);
            dto.setAuthor(isAuthor);

            // 언급 유저 정보 포함
            dto.setMentionedUser(comment.getMentionedUser());

            return dto;
        });
    }



    // 댓글 수정
    @Transactional
    public void update(String authorizationHeader, Integer commentId, CommentUpdateRequest commentUpdateRequest) {
        // JWT 토큰을 통해 인증된 회원 정보 조회
        User user = userService.validateUser(authorizationHeader);

        // 댓글 id로 댓글 조회
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException("댓글이 존재하지 않습니다."));

        // 작성자 본인인지 확인
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new UnAuthorizedAccessException("작성자만 댓글을 수정할 수 있습니다.");
        }

        // 댓글 내용 수정 및 저장
        comment.setCommentContent(commentUpdateRequest.getContent());
        commentRepository.save(comment);
    }


    // 댓글 삭제
    @Transactional
    public void delete(String authorizationHeader, Integer commentId) {
        // JWT 토큰을 통해 인증된 회원 정보 조회
        User user = userService.validateUser(authorizationHeader);

        // 댓글 id로 조회
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException("댓글이 존재하지 않습니다."));

        // 작성자 본인인지 확인
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new UnAuthorizedAccessException("작성자만 댓글을 수정할 수 있습니다.");
        }

        // 댓글 삭제
        comment.setDeletedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }


    // 댓글 좋아요
    @Transactional
    public CommentLikeResponse likeComment(String authorizationHeader, Integer commentId, CommentLikeRequest request) {
        // JWT 토큰을 통해 인증된 회원 정보 조회
        User user = userService.validateUser(authorizationHeader);


        // 댓글 찾기
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException("댓글이 존재하지 않습니다.");
        }

        // 댓글 좋아요 있는지 찾기
        CommentLikeId commentLikeId = new CommentLikeId(commentId, user.getId());
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