package com.sparta.todayeats;

import com.sparta.todayeats.ai.api.gemini.client.GeminiClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class TodayEatsApplicationTests {

    @MockitoBean
    JavaMailSender javaMailSender;

    @MockitoBean
    GeminiClient geminiClient;

    @Test
    void contextLoads() {
    }
}