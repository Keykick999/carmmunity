package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.dto.*;
import com.example.carmmunity_capstone.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }


    // 댓글 저장
    @PostMapping
    public ResponseEntity<String> saveComment(@RequestBody CommentSaveRequest commentSaveRequest
                                              ) {
        commentService.save(commentSaveRequest);
        return ResponseEntity.ok("댓글이 성공적으로 작성되었습니다!");
    }


    // 게시글 id로 댓글 조회
    @GetMapping
    public ResponseEntity<Page<CommentSearchResponse>> getCommentsByPostId(@RequestParam(value = "parentCommentId", required = false) Integer parentCommentId,
                                                                           @RequestParam(value = "postId") Integer postId,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "10") int size,
                                                                           @RequestParam(defaultValue = "CREATEDAT") CommentOrderBy orderBy,
                                                                           @RequestParam(defaultValue = "DESC")Sort.Direction direction) {
        return ResponseEntity.ok(commentService.getComments(postId, parentCommentId, page, size, orderBy, direction));
    }



    // 댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<String> updateComment(@PathVariable("commentId") Integer commentId,
                                                @RequestBody CommentUpdateRequest commentUpdateRequest
                                                ) {
        commentService.update(commentId, commentUpdateRequest);
        return ResponseEntity.ok("댓글이 성공적으로 수정되었습니다.");
    }


    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable("commentId") Integer commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }


    // 댓글 좋아요
    @PostMapping("/{commentId}/like")
    public ResponseEntity<CommentLikeResponse> likeComment(@PathVariable("commentId") Integer commentId,
                                                           @RequestBody CommentLikeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.likeComment(commentId, request));
    }
}
