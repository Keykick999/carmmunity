package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    @Modifying
    @Query("UPDATE Post p SET p.totalLike = p.totalLike + 1 WHERE p.postId = :postId")
    void incrementPostLike(@Param("postId") Integer postId);

    @Modifying
    @Query("UPDATE Post p SET p.totalLike = p.totalLike - 1 WHERE p.postId = :postId")
    void decrementPostLike(@Param("postId") Integer postId);
}
