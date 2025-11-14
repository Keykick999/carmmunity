package com.example.carmmunity_capstone.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserCarId implements Serializable {

    @Column(name = "user_uuid")
    private String userId;

    @Column(name = "car_id")
    private Integer carId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCarId)) return false;
        UserCarId that = (UserCarId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(carId, that.carId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, carId);
    }
}
