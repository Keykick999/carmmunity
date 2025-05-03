package com.example.carmmunity_capstone.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class UserCarId implements Serializable {

    @Column(name = "User_ID")
    private String userId;

    @Column(name = "Car_ID")
    private Integer carId;

    // equals(), hashCode() 구현
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
