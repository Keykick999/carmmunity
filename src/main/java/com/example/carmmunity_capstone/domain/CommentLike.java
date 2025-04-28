package com.example.carmmunity_capstone.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentLike {
    @EmbeddedId
    private CommentLikeId id;

    @Column(nullable = false)
    private Boolean likeStatus = true;
}
