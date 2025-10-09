package com.iemr.common.utils.response;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private Integer statusCode;

    private T data;

    public ApiResponse() {}
    
    public ApiResponse(boolean success, String message, Integer statusCode,T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.statusCode = statusCode;
    }

    public static <T> ApiResponse<T> success(String message,Integer statusCode, T data) {
        return new ApiResponse<>(true, message,statusCode, data);
    }

    public static <T> ApiResponse<T> error(String message,Integer statusCode, T data) {
        return new ApiResponse<>(false, message, statusCode,data);
    }

    // getters and setters
}
