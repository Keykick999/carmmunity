package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.domain.Post;
import com.example.carmmunity_capstone.dto.PostSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    @Modifying
    @Query("UPDATE Post p SET p.totalLike = p.totalLike + 1 WHERE p.postId = :postId")
    void incrementPostLike(@Param("postId") Integer postId);

    @Modifying
    @Query("UPDATE Post p SET p.totalLike = p.totalLike - 1 WHERE p.postId = :postId")
    void decrementPostLike(@Param("postId") Integer postId);

    @Query("SELECT new com.example.carmmunity_capstone.dto.PostSearchResponse(" +
            "p.postId, p.postTitle, p.postType, p.postContent, p.imagePath, p.totalLike, u.nickname, p.updatedAt) " +
            "FROM Post p " +
            "JOIN p.user u " +
            "WHERE p.postId = :postId " +
            "and p.deletedAt is null")
    Optional<PostSearchResponse> findPostById(@Param("postId") Integer postId);


    @Query("SELECT new com.example.carmmunity_capstone.dto.PostSearchResponse(" +
            "p.postId, p.postTitle, p.postType, p.postContent, p.imagePath, p.totalLike, u.nickname, p.updatedAt) " +
            "FROM Post p " +
            "JOIN p.user u " +
            "WHERE p.deletedAt is null")
    Page<PostSearchResponse> findAllPosts(Pageable pageable);
}
