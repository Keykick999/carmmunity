package com.example.carmmunity_capstone.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeId implements Serializable {
    private Integer postId;

    private String userUuid; // userId -> userUuid

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostLikeId)) return false;
        PostLikeId that = (PostLikeId) o;
        return Objects.equals(postId, that.postId) &&
                Objects.equals(userUuid, that.userUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, userUuid);
    }
}