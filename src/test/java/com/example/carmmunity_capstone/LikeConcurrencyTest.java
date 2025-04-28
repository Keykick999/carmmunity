package com.example.carmmunity_capstone;

import com.example.carmmunity_capstone.dto.CommentLikeRequest;
import com.example.carmmunity_capstone.jwt.JwtProvider;
import com.example.carmmunity_capstone.repository.CommentRepository;
import com.example.carmmunity_capstone.service.CommentService;
import com.example.carmmunity_capstone.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class LikeConcurrencyTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProvider jwtProvider;

    private static final int THREAD_COUNT = 100;
    private final String[] testTokens = new String[THREAD_COUNT]; // 100개 토큰
    private final Integer commentId = 4;    // 테스트용 댓글 id



    @BeforeEach
    void setup() {
        // 100명의 유저 토큰 발급
        for (int i = 0; i < THREAD_COUNT; i++) {
            String userId = String.format("user_%03d", i + 1); // user_001, user_002, ...
            String role = "user";
            testTokens[i] = jwtProvider.createToken(userId, role);
        }
    }

    @Test
    void 동시에_여러_사용자가_댓글에_좋아요를_누르면_정확히_반영되어야_한다() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        CommentLikeRequest request = new CommentLikeRequest();
        request.setLikeStatus(true);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int idx = i; // 람다에서 i 바로 쓰면 안 됨
            executorService.submit(() -> {
                try {
                    commentService.likeComment(testTokens[idx], commentId, request);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 끝날 때까지 대기

        int likeCount = commentRepository.findById(commentId)
                .orElseThrow()
                .getTotalLike();

        System.out.println("최종 좋아요 수: " + likeCount);
        assertEquals(THREAD_COUNT, likeCount);
    }
}
