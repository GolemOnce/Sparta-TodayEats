package com.sparta.todayeats.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "카테고리 생성 요청")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCreateRequest {
    @Schema(description = "카테고리 이름", example = "한식")
    @NotBlank(message = "카테고리 이름은 필수입니다.")
    @Size(max = 50, message = "카테고리 이름은 50자 이하여야 합니다.")
    private String name;
}
