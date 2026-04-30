package com.sparta.todayeats.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "공통 응답 객체")
@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    @Schema(description = "HTTP 상태 코드", example = "200")
    private int status;

    @Schema(description = "응답 메시지", example = "SUCCESS")
    private String message;

    @Schema(description = "응답 데이터")
    private T data;

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "CREATED", data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "SUCCESS", data);
    }
}
