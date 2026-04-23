package com.sparta.todayeats.global.init;

import com.sparta.todayeats.area.domain.entity.Area;
import com.sparta.todayeats.area.domain.repository.AreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final AreaRepository areaRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (areaRepository.existsByNameIgnoreCase("광화문")) return; // 이미 있으면 스킵

        areaRepository.save(Area.builder()
                .name("광화문")
                .city("서울특별시")
                .district("종로구")
                .isActive(true)
                .build());
    }
}
