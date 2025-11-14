package com.example.carmmunity_capstone.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserTermId implements Serializable {

    private String userUuid; // userId -> userUuid
    private String termCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserTermId)) return false;
        UserTermId that = (UserTermId) o;
        return Objects.equals(userUuid, that.userUuid) &&
                Objects.equals(termCode, that.termCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userUuid, termCode);
    }
}