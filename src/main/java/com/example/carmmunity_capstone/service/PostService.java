package com.example.carmmunity_capstone.service;
import com.example.carmmunity_capstone.entity.Post;
import com.example.carmmunity_capstone.entity.PostLike;
import com.example.carmmunity_capstone.entity.PostLikeId;
import com.example.carmmunity_capstone.dto.*;
import com.example.carmmunity_capstone.entity.User;
import com.example.carmmunity_capstone.exception.PostNotFoundException;
import com.example.carmmunity_capstone.exception.UnAuthorizedAccessException;
import com.example.carmmunity_capstone.repository.PostLikeRepository;
import com.example.carmmunity_capstone.repository.PostRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;

    private final PostLikeRepository postLikeRepository;

    private final UserService userService;

    @Value("${thumbnail.default-path}")
    private String defaultThumbnailPath;

    public PostService(PostRepository postRepository, PostLikeRepository postLikeRepository, UserService userService) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.userService = userService;
    }


    // TODO 전부 user 관련 부분 추가해야 관련 저장 값이나 권한 확인 전부
    // 게시글 저장
    @Transactional
    public void save( PostRequest postSaveRequest, MultipartFile postImage, MultipartFile thumbnailImage) {
        User user = userService.getAuthenticatedUser();


        // 게시글 저장
        Post post = Post.builder()
                .deletedAt(null)
                .totalCount(0)
                .totalLike(0)
                .user(user)
                .build();
        post = applyPostData(post, postSaveRequest, postImage, thumbnailImage);


        postRepository.save(post);
    }


    // 게시글 전체 조회
    @Transactional(readOnly = true)
    public Page<PostSearchResponse> findAll(PostSearchCondition condition, int page, int size, PostOrderBy orderBy, Sort.Direction direction) {
        Sort sort = Sort.by(direction, orderBy.getField());
        Pageable pageable = PageRequest.of(page, size, sort);

        String title = condition.getTitle();
        String nickname = condition.getNickname();

        if (title != null && !title.isBlank() && nickname != null && !nickname.isBlank()) {
            return postRepository.findByTitleAndNickname(title, nickname, pageable);
        } else if (title != null && !title.isBlank()) {
            return postRepository.findByTitle(title, pageable);
        } else if (nickname != null && !nickname.isBlank()) {
            return postRepository.findByNickname(nickname, pageable);
        } else {
            return postRepository.findAllPosts(pageable);
        }
    }


    // 게시글 id로 조회
    @Transactional(readOnly = true)
    public PostSearchResponse findById(int postId) {
        User currentUser = userService.getAuthenticatedUser();

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .filter(p -> p.getDeletedAt() == null) // 삭제 안 된 게시글만 허용
                .orElseThrow(() -> new PostNotFoundException("해당 게시글을 찾을 수 없습니다."));

        // 좋아요 여부 확인
        PostLikeId likeId = new PostLikeId(postId, currentUser.getUserId());
        boolean liked = postLikeRepository.findById(likeId)
                .map(PostLike::getLikeStatus)
                .orElse(false);

        boolean isAuthor = post.getUser().getUserId().equals(currentUser.getUserId());

        return new PostSearchResponse(
                post.getPostId(),
                post.getPostTitle(),
                post.getPostType(),
                post.getPostContent(),
                post.getImagePath(),
                post.getThumbnailPath(),
                post.getTotalLike(),
                post.getUser().getNickname(),
                post.getUpdatedAt(),
                liked,
                isAuthor
        );
    }



    // 게시글 수정(저장이랑 동일하게 처리?)
    @Transactional
    public void update(Integer postId, PostRequest updateRequest, MultipartFile postImage, MultipartFile thumbnailImage) {
        User user = userService.getAuthenticatedUser();

        // 게시글 존재 확인
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("해당 게시글을 찾을 수 없습니다"));

        // 작성자 본인인지 확인
        if (!post.getUser().getUserId().equals(user.getUserId())) {
            throw new UnAuthorizedAccessException("작성자만 게시글을 수정할 수 있습니다.");
        }

        // 게시글 수정 후 저장
        post = applyPostData(post, updateRequest, postImage, thumbnailImage);
        postRepository.save(post);
    }


    // 게시글 삭제
    @Transactional
    public void delete(Integer postId) {
        User user = userService.getAuthenticatedUser();

        // 게시글 찾기
        Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("해당 게시글을 찾을 수 없습니다."));

        // 작성자 본인인지 확인
        if (!post.getUser().getUserId().equals(user.getUserId())) {
            throw new UnAuthorizedAccessException("작성자만 게시글을 수정할 수 있습니다.");
        }

        // 게시글 삭제 표시
        post.setDeletedAt(LocalDateTime.now());
        postRepository.save(post);
    }


    // 이미지 저장
    public String saveImage(MultipartFile imageFile) {
        // 1. 저장할 실제 경로
        Path path = Paths.get("C:", "uploads"); // 경로 변경: "C:/uploads"
        String uploadDir = path.toString();

        String storedFileName = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // 2. 파일명 처리
                String originalFileName = imageFile.getOriginalFilename();
                String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
                storedFileName = UUID.randomUUID().toString() + ext;

                // 3. 실제 저장
                File dest = new File(uploadDir, storedFileName);
                if (!dest.getParentFile().exists()) {
                    dest.getParentFile().mkdirs();
                }
                imageFile.transferTo(dest);


                // ✅ 4. DB에는 상대 경로로 저장
                return "/uploads/" + storedFileName;

            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 중 오류 발생", e);
            }
        }

        return null;
    }


    // 게시글 요청 값 설정 및 이미지 저장
    public Post applyPostData(Post post, PostRequest request, MultipartFile imageFile, MultipartFile thumbnailImage) {
        // 메인 이미지 저장
        String savedImagePath = saveImage(imageFile);
        post.setImagePath(savedImagePath);

        // 썸네일 이미지 저장 (없으면 기본값 적용)
        String savedThumbnailPath = saveImage(thumbnailImage);
        if (savedThumbnailPath == null || savedThumbnailPath.isBlank()) {
            post.setThumbnailPath(defaultThumbnailPath);
        } else {
            post.setThumbnailPath(savedThumbnailPath);
        }

        // 나머지 게시글 정보 설정
        post.setPostTitle(request.getTitle());
        post.setPostContent(request.getContent());
        post.setPostType(request.getPostType());

        return post;
    }


    // 게시글 좋아요
    @Transactional
    public PostLikeResponse likePost(Integer postId, PostLikeRequest request) {
        User user = userService.getAuthenticatedUser();

        // 게시글 찾기
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("게시글이 존재하지 않습니다.");
        }

        // 게시글 좋아요 있는지 찾기
        PostLikeId postLikeId = new PostLikeId(postId, user.getUserId());
        PostLike postLike = toggleOrCreateLike(postLikeId);
        postLikeRepository.save(postLike);

        // 게시글 테이블에 좋아요 수 반영
        if (postLike.getLikeStatus()) {
            postRepository.incrementPostLike(postId);
        } else {
            postRepository.decrementPostLike(postId);
        }

        return new PostLikeResponse(postLike.getLikeStatus());
    }

    private PostLike toggleOrCreateLike(PostLikeId id) {
        return postLikeRepository.findById(id)
                .map(postLike -> {
                    postLike.setLikeStatus(!postLike.getLikeStatus());
                    return postLike;
                })
                .orElse(new PostLike(id, true));
    }


    @Transactional(readOnly = true)
    public Resource getPostThumbnail(Integer postId) throws MalformedURLException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글을 찾을 수 없습니다."));

        String imagePath = post.getThumbnailPath();
        if (imagePath == null || imagePath.isBlank()) {
            throw new PostNotFoundException("게시글에 이미지가 존재하지 않습니다.");
        }

        Path path = Paths.get(imagePath);
        return new UrlResource(path.toUri());
    }
}