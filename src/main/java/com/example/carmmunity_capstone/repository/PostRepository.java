package com.example.carmmunity_capstone.repository;
import com.example.carmmunity_capstone.entity.Post;
import com.example.carmmunity_capstone.dto.PostSearchResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.postId = :postId")
    Post findByIdForUpdate(@Param("postId") Integer postId);


    @Query("SELECT new com.example.carmmunity_capstone.dto.PostSearchResponse(" +
            "p.postId, p.postTitle, p.postType, p.postContent, p.imagePath, p.thumbnailPath, p.totalLike, u.nickname, p.updatedAt, false, false) " +
            "FROM Post p " +
            "JOIN p.user u " +
            "WHERE p.deletedAt is null")
    Page<PostSearchResponse> findAllPosts(Pageable pageable);



    @Query("SELECT new com.example.carmmunity_capstone.dto.PostSearchResponse(" +
            "p.postId, p.postTitle, p.postType, p.postContent, p.imagePath, p.thumbnailPath, p.totalLike, u.nickname, p.updatedAt, false, false) " +
            "FROM Post p " +
            "JOIN p.user u " +
            "WHERE p.deletedAt is null AND LOWER(p.postTitle) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<PostSearchResponse> findByTitle(String title, Pageable pageable);

    @Query("SELECT new com.example.carmmunity_capstone.dto.PostSearchResponse(" +
            "p.postId, p.postTitle, p.postType, p.postContent, p.imagePath, p.thumbnailPath, p.totalLike, u.nickname, p.updatedAt, false, false) " +
            "FROM Post p " +
            "JOIN p.user u " +
            "WHERE p.deletedAt is null AND LOWER(u.nickname) LIKE LOWER(CONCAT('%', :nickname, '%'))")
    Page<PostSearchResponse> findByNickname(String nickname, Pageable pageable);

    @Query("SELECT new com.example.carmmunity_capstone.dto.PostSearchResponse(" +
            "p.postId, p.postTitle, p.postType, p.postContent, p.imagePath, p.thumbnailPath, p.totalLike, u.nickname, p.updatedAt, false, false) " +
            "FROM Post p " +
            "JOIN p.user u " +
            "WHERE p.deletedAt is null " +
            "AND LOWER(p.postTitle) LIKE LOWER(CONCAT('%', :title, '%')) " +
            "AND LOWER(u.nickname) LIKE LOWER(CONCAT('%', :nickname, '%'))")
    Page<PostSearchResponse> findByTitleAndNickname(String title, String nickname, Pageable pageable);
}
