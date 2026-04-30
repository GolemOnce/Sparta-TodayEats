package com.sparta.todayeats.menu.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sparta.todayeats.menu.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "메뉴 정보 응답")
@Getter
@Builder
public class MenuDetailResponse {
    @Schema(description = "메뉴 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("menu_id")
    private UUID menuId;

    @Schema(description = "가게 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @JsonProperty("store_id")
    private UUID storeId;

    @Schema(description = "메뉴 이름", example = "고기듬뿍 고향만두")
    private String name;

    @Schema(description = "메뉴 설명", example = "국산 돼지고기와 신선한 야채가 어우러진 육즙 가득 만두")
    private String description;

    @Schema(description = "가격", example = "5500")
    private int price;

    @Schema(description = "품절 여부", example = "false")
    @JsonProperty("is_sold_out")
    private boolean soldOut;

    @Schema(description = "생성 시간", example = "2026-04-30T10:00:00")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "생성자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("created_by")
    private String createdBy;

    @Schema(description = "수정 시간", example = "2026-04-30T12:00:00")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Schema(description = "수정자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("updated_by")
    private String updatedBy;

    public static MenuDetailResponse from(Menu menu) {
        return MenuDetailResponse.builder()
                .menuId(menu.getId())
                .storeId(menu.getStore().getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .price(Math.toIntExact(menu.getPrice()))
                .soldOut(menu.isSoldOut())
                .createdAt(menu.getCreatedAt())
                .createdBy(String.valueOf(menu.getCreatedBy()))
                .updatedAt(menu.getUpdatedAt())
                .updatedBy(String.valueOf(menu.getUpdatedBy()))
                .build();
    }
}