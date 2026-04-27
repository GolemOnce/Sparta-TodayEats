package com.sparta.todayeats;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class TodayEatsApplicationTests {

    @MockitoBean
    JavaMailSender javaMailSender;

    @Test
    void contextLoads() {
    }
}