package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.dto.CommentSearchResponse;
import com.example.carmmunity_capstone.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    // 게시글 id로 댓글 조회
    @Query("SELECT new com.example.carmmunity_capstone.dto.CommentSearchResponse(" +
            "c.commentId, c.commentContent, c.totalLike, " +
            "CASE WHEN c.user.userId = :currentUserId THEN true ELSE false END, " +
            "c.user.nickname) " +
            "FROM Comment c " +
            "WHERE c.deletedAt IS NULL " +
            "AND c.post.postId = :postId " +
            "AND ((:parentId IS NULL AND c.parentComment IS NULL) " +
            "   OR (:parentId IS NOT NULL AND c.parentComment.commentId = :parentId))")
    Page<CommentSearchResponse> findCommentsByPostAndParent(@Param("postId") Integer postId,
                                                            @Param("parentId") Integer parentId,
                                                            @Param("currentUserId") String currentUserId,
                                                            Pageable pageable);



    @Modifying
    @Query("UPDATE Comment c SET c.totalLike = c.totalLike + 1 WHERE c.commentId = :commentId")
    void incrementCommentLike(@Param("commentId") Integer commentId);

    @Modifying
    @Query("UPDATE Comment c SET c.totalLike = c.totalLike - 1 WHERE c.commentId = :commentId")
    void decrementCommentLike(@Param("commentId") Integer commentId);
}
