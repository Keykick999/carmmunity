package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.domain.CommentLike;
import com.example.carmmunity_capstone.domain.CommentLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {
}