package com.example.carmmunity_capstone.dto;

public enum PostOrderBy {
    CREATEDAT("createdAt"),
    UPDATEDAT("updatedAt"),
    LIKECOUNT("totalLike");

    private final String field;

    PostOrderBy(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}

