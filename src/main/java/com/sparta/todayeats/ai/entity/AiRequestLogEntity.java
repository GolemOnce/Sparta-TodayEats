package com.sparta.todayeats.ai.entity;

import com.sparta.todayeats.global.infrastructure.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_ai_request_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRequestLogEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ai_request_log_id", nullable = false, updatable = false)
    private String id;

    // 사용자 원본
    @Column(nullable = false, length = 100)
    private String prompt;

    // 가공 된 프롬프트 (추후에 응답 로직 트레이싱 가능)
    @Column(name = "request_prompt", nullable = false, length = 300)
    private String requestPrompt;

    // 결과
    @Column(nullable = false, length = 500)
    private String response;

    @Column(name = "user_id")
    private UUID userId;

    public AiRequestLogEntity(String prompt, String requestPrompt, String response, UUID userId) {
        this.prompt = prompt;
        this.requestPrompt = requestPrompt;
        this.response = response;
        this.userId = userId;
    }
}
