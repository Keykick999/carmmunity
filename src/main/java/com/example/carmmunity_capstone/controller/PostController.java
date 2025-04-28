package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.domain.Post;
import com.example.carmmunity_capstone.dto.PostLikeRequest;
import com.example.carmmunity_capstone.dto.PostLikeResponse;
import com.example.carmmunity_capstone.dto.PostRequest;
import com.example.carmmunity_capstone.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }


    // 게시글 작성
    @PostMapping("/posts")
    public ResponseEntity<String> save(@RequestHeader(value = "Authorization", required = true)  String authorizationHeader,
                                       @RequestPart("postData") PostRequest postSaveRequest,
                                       @RequestPart(value = "postImage", required = false) MultipartFile postImage) {
        postService.save(authorizationHeader, postSaveRequest, postImage);
        return ResponseEntity.status(HttpStatus.CREATED).body("게시글이 성공적으로 작성되었습니다!");
    }


    // 게시글 전체 조회
    @GetMapping
    public ResponseEntity<Page<Post>> findAll(@RequestParam(value = "page", defaultValue = "0") int page,
                                              @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.findAll(page, size));
    }


    // 게시글 단일 조회
    @GetMapping("/{postId}")
    public ResponseEntity<Post> findById(@PathVariable("postId") int postId) {
        return ResponseEntity.ok(postService.findById(postId));
    }


    // 게시글 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<String> updatePost(@RequestHeader(value = "Authorization", required = true)  String authorizationHeader,
                                             @PathVariable("postId") Integer postId,
                                             @RequestPart("postData") PostRequest postUpdateRequest,
                                             @RequestPart(value = "postImage", required = false) MultipartFile postImage) {
        postService.update(authorizationHeader, postId, postUpdateRequest, postImage);
        return ResponseEntity.status(HttpStatus.CREATED).body("게시글이 성공적으로 수정되었습니다!");
    }



    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@RequestHeader(value = "Authorization", required = true) String authorizationHeader,
                                             @PathVariable("postId") Integer postId) {
        postService.delete(authorizationHeader, postId);
        return ResponseEntity.noContent().build();
    }


    // 게시글 좋아요
    @PostMapping("/{postId}/like")
    public ResponseEntity<PostLikeResponse> likePost(@RequestHeader("Authorization") String authorizationHeader,
                                                     @PathVariable("postId") Integer postId,
                                                     @RequestBody PostLikeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.likePost(authorizationHeader, postId, request));
    }
}












