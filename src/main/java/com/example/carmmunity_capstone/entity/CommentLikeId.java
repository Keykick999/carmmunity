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
public class CommentLikeId implements Serializable {
    private Integer commentId;

    private String userUuid; // userId -> userUuid

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommentLikeId)) return false;
        CommentLikeId that = (CommentLikeId) o;
        return Objects.equals(commentId, that.commentId) &&
                /**
                 * [수정] userUuid로 변경
                 */
                Objects.equals(userUuid, that.userUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentId, userUuid);
    }
}