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

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {

    // QueryDSL 쿼리를 실행하는 핵심 객체
    // QueryDslConfig에서 빈으로 등록한 것을 주입받음
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Store> searchStores(String categoryName, String keyword, Pageable pageable, boolean isCustomer) {

        // compileJava로 자동 생성된 Q클래스 (Store 엔티티 기반)
        // 타입 안전하게 컬럼명, 조건 등을 작성할 수 있게 해줌
        QStore store = QStore.store;

        // 동적 조건을 조합하는 빌더 (AND)
        BooleanBuilder builder = new BooleanBuilder();

        // CUSTOMER or 비로그인 → 공개된 가게만 노출
        // OWNER / MANAGER / MASTER → 숨긴 가게 포함 전체 노출
        if (isCustomer) {
            builder.and(store.isHidden.isFalse());
        }

        // 카테고리 이름으로 필터
        if (categoryName != null && !categoryName.isBlank()) {
            builder.and(store.category.name.containsIgnoreCase(categoryName));
        }

        // 이름 검색
        if (keyword != null && !keyword.isBlank()) {
            builder.and(store.name.containsIgnoreCase(keyword));
        }

        // 실제 데이터 조회 (페이징 적용)
        List<Store> content = queryFactory
                .selectFrom(store)      // SELECT * FROM p_store
                .where(builder)         // WHERE 동적 조건
                .offset(pageable.getOffset())       // 시작 위치 (page * size)
                .limit(pageable.getPageSize())       // 가져올 개수
                .orderBy(store.createdAt.desc())     // 최신순 정렬
                .fetch();

        // 전체 데이터 개수 조회 (totalPages 계산에 필요)
        Long total = queryFactory
                .select(store.count())
                .from(store)
                .where(builder)
                .fetchOne();

        // Page 객체로 감싸서 반환
        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }
}
