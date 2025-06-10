package com.example.carmmunity_capstone.service;

import com.example.carmmunity_capstone.entity.*;
import com.example.carmmunity_capstone.dto.*;
import com.example.carmmunity_capstone.exception.CommentNotFoundException;
import com.example.carmmunity_capstone.exception.PostNotFoundException;
import com.example.carmmunity_capstone.exception.UnAuthorizedAccessException;
import com.example.carmmunity_capstone.repository.CommentLikeRepository;
import com.example.carmmunity_capstone.repository.CommentRepository;
import com.example.carmmunity_capstone.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final UserService userService;



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
    public Page<CommentSearchResponse> getComments(Integer postId, Integer parentCommentId, int page, int size, CommentOrderBy orderBy, Sort.Direction direction) {
        Optional<User> userOpt = userService.getUserIfAuthenticated();
        String userId = userOpt.map(User::getUserId).orElse(null);

        Sort sort = Sort.by(direction, orderBy.getField());
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Comment> commentPage = commentRepository.findCommentsByPostAndParent(postId, parentCommentId, pageable);

        return commentPage.map(comment -> {
            boolean liked = false;
            if (userId != null) {
                CommentLikeId id = new CommentLikeId(comment.getCommentId(), userId);
                liked = commentLikeRepository.findById(id)
                        .map(CommentLike::getLikeStatus)
                        .orElse(false);
            }

            return new CommentSearchResponse(
                    comment.getCommentId(),
                    comment.getCommentContent(),
                    comment.getTotalLike(),
                    userId != null && userId.equals(comment.getUser().getUserId()),
                    comment.getUser().getNickname(),
                    liked
            );
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
        User user = userService.getAuthenticatedUser();

        // 🚨 기존의 existsById 제거하고, 락 걸고 가져오기
        Comment comment = commentRepository.findByIdForUpdate(commentId)
                .orElseThrow(() -> new CommentNotFoundException("댓글이 존재하지 않습니다."));

        CommentLikeId commentLikeId = new CommentLikeId(commentId, user.getUserId());
        CommentLike commentLike = toggleOrCreateLike(commentLikeId);
        commentLikeRepository.save(commentLike);

        // 🚨 동시에 comment 수정
        if (commentLike.getLikeStatus()) {
            comment.setTotalLike(comment.getTotalLike() + 1);
        } else {
            comment.setTotalLike(comment.getTotalLike() - 1);
        }
        commentRepository.save(comment);

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