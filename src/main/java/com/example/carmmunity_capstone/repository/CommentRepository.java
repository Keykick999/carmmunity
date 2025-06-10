package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.entity.Comment;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    // 게시글 id로 댓글 조회
    @Query("SELECT c FROM Comment c " +
            "WHERE c.deletedAt IS NULL " +
            "AND c.post.postId = :postId " +
            "AND ((:parentId IS NULL AND c.parentComment IS NULL) " +
            "   OR (:parentId IS NOT NULL AND c.parentComment.commentId = :parentId))")
    Page<Comment> findCommentsByPostAndParent(@Param("postId") Integer postId,
                                              @Param("parentId") Integer parentId,
                                              Pageable pageable);



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Comment c WHERE c.commentId = :commentId")
    Optional<Comment> findByIdForUpdate(@Param("commentId") Integer commentId);



    @Modifying
    @Query("UPDATE Comment c SET c.totalLike = c.totalLike + 1 WHERE c.commentId = :commentId")
    void incrementCommentLike(@Param("commentId") Integer commentId);

    @Modifying
    @Query("UPDATE Comment c SET c.totalLike = c.totalLike - 1 WHERE c.commentId = :commentId")
    void decrementCommentLike(@Param("commentId") Integer commentId);
}
