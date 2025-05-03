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
    public ResponseEntity<String> saveComment(@RequestHeader(value = "Authorization", required = true) String authorizationHeader,
                                              @RequestBody CommentSaveRequest commentSaveRequest
                                              ) {
        commentService.save(authorizationHeader, commentSaveRequest);
        return ResponseEntity.ok("댓글이 성공적으로 작성되었습니다!");
    }


    // 게시글 id로 댓글 조회
    @GetMapping
    public ResponseEntity<Page<CommentSearchResponse>> getCommentsByPostId(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                                                           @RequestParam("parentCommentId") Integer parentCommentId,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "10") int size,
                                                                           @RequestParam(defaultValue = "CREATEDAT") CommentOrderBy orderBy,
                                                                           @RequestParam(defaultValue = "DESC")Sort.Direction direction) {
        return ResponseEntity.ok(commentService.getComments(authorizationHeader, parentCommentId, page, size, orderBy, direction));
    }



    // 댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<String> updateComment(@RequestHeader(value = "authorization", required = true) String authorizationHeader,
                                                @PathVariable("commentId") Integer commentId,
                                                @RequestBody CommentUpdateRequest commentUpdateRequest
                                                ) {
        commentService.update(authorizationHeader, commentId, commentUpdateRequest);
        return ResponseEntity.ok("댓글이 성공적으로 수정되었습니다.");
    }


    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@RequestHeader(value = "Authorization", required = true) String authorizationHeader,
                                                @PathVariable("commentId") Integer commentId) {
        commentService.delete(authorizationHeader, commentId);
        return ResponseEntity.noContent().build();
    }


    // 댓글 좋아요
    @PostMapping("/{commentId}/like")
    public ResponseEntity<CommentLikeResponse> likeComment(@RequestHeader("Authorization") String authorizationHeader,
                                                           @PathVariable("commentId") Integer commentId,
                                                           @RequestBody CommentLikeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.likeComment(authorizationHeader, commentId, request));
    }
}





