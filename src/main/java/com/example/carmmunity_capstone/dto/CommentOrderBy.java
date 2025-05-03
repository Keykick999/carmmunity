package com.example.carmmunity_capstone.dto;

public enum CommentOrderBy {
    CREATEDAT("createdAt"),
    UPDATEDAT("updatedAt"),
    LIKECOUNT("totalLike");

    private final String field;

    CommentOrderBy(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
