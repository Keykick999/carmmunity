package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.entity.PostLike;
import com.example.carmmunity_capstone.entity.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
}
