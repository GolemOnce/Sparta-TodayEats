package com.sparta.todayeats.store.repository;

import com.sparta.todayeats.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.UUID;


// QueryDSL로 구현할 커스텀 쿼리 메서드 정의
// StoreRepository가 이 인터페이스를 상속받아서 사용
public interface StoreRepositoryCustom {
    Page<Store> searchStores(String categoryName, String keyword, Pageable pageable, Authentication authentication, UUID userId);
}
