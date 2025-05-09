package com.example.carmmunity_capstone.repository;

import com.example.carmmunity_capstone.entity.UserCar;
import com.example.carmmunity_capstone.entity.UserCarId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface UserCarRepository extends JpaRepository<UserCar, UserCarId> {
    @Query("select count(u) from UserCar u where u.user.id = :userId and u.deletedAt is null")
    int countUserCars(@Param("userId") String userId);

    boolean existsByIdAndDeletedAtIsNull(UserCarId id);

    Optional<UserCar> findByIdAndDeletedAtIsNull(UserCarId id);
}
