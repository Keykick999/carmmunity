package com.example.carmmunity_capstone.controller;

import com.example.carmmunity_capstone.dto.*;
import com.example.carmmunity_capstone.service.PostService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }


    // 게시글 작성
    @PostMapping
    public ResponseEntity<String> save(
            @RequestPart("postData") PostRequest postSaveRequest,
            @RequestPart(value = "postImage", required = false) MultipartFile postImage,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) {
        postService.save(postSaveRequest, postImage, thumbnailImage);
        return ResponseEntity.status(HttpStatus.CREATED).body("게시글이 성공적으로 작성되었습니다!");
    }



    // 게시글 전체 조회
    @GetMapping
    public ResponseEntity<Page<PostSearchResponse>> findAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(defaultValue = "UPDATEDAT") PostOrderBy orderBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String nickname
    ) {
        PostSearchCondition condition = new PostSearchCondition(title, nickname);
        return ResponseEntity.ok(postService.findAll(condition, page, size, orderBy, direction));
    }



    // 게시글 단일 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostSearchResponse> findById(@PathVariable("postId") int postId) {
        return ResponseEntity.ok(postService.findById(postId));
    }


    // 게시글 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<String> updatePost(@PathVariable("postId") Integer postId,
                                             @RequestPart("postData") PostRequest postUpdateRequest,
                                             @RequestPart(value = "postImage", required = false) MultipartFile postImage,
                                             @RequestPart(value = "postImage", required = false) MultipartFile thumbnailImage) {
        postService.update(postId, postUpdateRequest, postImage, thumbnailImage);
        return ResponseEntity.status(HttpStatus.CREATED).body("게시글이 성공적으로 수정되었습니다!");
    }



    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable("postId") Integer postId) {
        postService.delete(postId);
        return ResponseEntity.noContent().build();
    }


    // 게시글 좋아요
    @PostMapping("/{postId}/like")
    public ResponseEntity<PostLikeResponse> likePost(@PathVariable("postId") Integer postId,
                                                     @RequestBody PostLikeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.likePost(postId, request));
    }


    // 게시글 이미지 반환
    @GetMapping("/image/{postId}")
    public ResponseEntity<Resource> getPostThumbnail(@PathVariable("postId") Integer postId) throws MalformedURLException {
        Resource image = postService.getPostThumbnail(postId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // 필요시 확장자에 따라 변경 가능
                .body(image);
    }
}












