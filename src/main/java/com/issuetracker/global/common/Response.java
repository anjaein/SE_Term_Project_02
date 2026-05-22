package com.issuetracker.global.common;

import lombok.Getter;

// Service·Controller 공통 응답 형식
@Getter
public class Response<T> {
    private final boolean success;
    private final String message;
    private final T data;

    private Response(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> Response<T> success(String message, T data) {
        return new Response<>(true, message, data);
    }

    public static <T> Response<T> success(String message) {
        return new Response<>(true, message, null);
    }

    public static <T> Response<T> fail(String message) {
        return new Response<>(false, message, null);
    }
}