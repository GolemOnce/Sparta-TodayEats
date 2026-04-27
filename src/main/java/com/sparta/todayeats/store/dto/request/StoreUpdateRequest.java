package com.sparta.todayeats.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 가게 수정 요청 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreUpdateRequest {

    @NotBlank(message = "가게 이름은 필수입니다")
    @Size(max = 100, message = "가게 이름은 100자 이내입니다")
    private String name;

    @NotBlank(message = "주소는 필수입니다")
    @Size(max = 255, message = "주소는 255자 이내입니다")
    private String address;

    @Size(max = 20, message = "전화번호는 20자 이내입니다")
    private String phone;
}
