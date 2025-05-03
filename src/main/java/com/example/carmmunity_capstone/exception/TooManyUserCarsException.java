package com.example.carmmunity_capstone.exception;

public class TooManyUserCarsException extends RuntimeException {
    public TooManyUserCarsException() {
        super("튜닝 차량은 최대 3대까지만 등록할 수 있습니다.");
    }

    public TooManyUserCarsException(String message) {
        super(message);
    }
}
