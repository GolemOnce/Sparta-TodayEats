package com.sparta.todayeats.store.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.todayeats.store.entity.QStore;
import com.sparta.todayeats.store.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {

    // QueryDSL 쿼리를 실행하는 핵심 객체
    // QueryDslConfig에서 빈으로 등록한 것을 주입받음
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Store> searchStores(String categoryName, String keyword, Pageable pageable, Authentication authentication, UUID userId) {

        // Q클래스, 빌더 (동적 조건 조합)
        QStore store = QStore.store;
        BooleanBuilder builder = new BooleanBuilder();


        // 카테고리 이름으로 필터
        if (categoryName != null && !categoryName.isBlank()) {
            builder.and(store.category.name.containsIgnoreCase(categoryName));
        }

        // 이름 검색
        if (keyword != null && !keyword.isBlank()) {
            builder.and(store.name.containsIgnoreCase(keyword));
        }

        // 권한 확인
        boolean isAnonymous = authentication == null;

        boolean isCustomer = authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

        boolean isOwner = authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        boolean isManagerOrMaster = authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")
                                || a.getAuthority().equals("ROLE_MASTER"));

        // 비로그인 / CUSTOMER → 공개만
        if (isAnonymous || isCustomer) {
            builder.and(store.isHidden.isFalse());
        }

        // OWNER → 공개 + 내 가게
        else if (isOwner) {
            builder.and(
                    store.isHidden.isFalse()
                            .or(store.owner.userId.eq(userId))
            );
        }

        // 실제 데이터 조회 (페이징 적용)
        List<Store> content = queryFactory
                .selectFrom(store)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(store.createdAt.desc())
                .fetch();

        long total = queryFactory
                .select(store.count())
                .from(store)
                .where(builder)
                .fetchOne();

        // Page 객체로 감싸서 반환
        return new PageImpl<>(content, pageable, total);
    }
}
