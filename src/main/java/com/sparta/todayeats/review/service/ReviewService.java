package com.sparta.todayeats.review.service;

import com.sparta.todayeats.global.exception.*;
import com.sparta.todayeats.order.Repository.OrderRepository;
import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.review.dto.request.ReviewCreateRequest;
import com.sparta.todayeats.review.dto.response.ReviewCreateResponse;
import com.sparta.todayeats.review.dto.response.ReviewDetailResponse;
import com.sparta.todayeats.review.dto.response.ReviewPageResponse;
import com.sparta.todayeats.review.entity.Review;
import com.sparta.todayeats.review.repository.ReviewRepository;
import com.sparta.todayeats.store.entity.Store;
import com.sparta.todayeats.store.repository.StoreRepository;
import com.sparta.todayeats.user.domain.entity.User;
import com.sparta.todayeats.user.domain.entity.UserRoleEnum;
import com.sparta.todayeats.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.sparta.todayeats.user.domain.entity.UserRoleEnum.MANAGER;
import static com.sparta.todayeats.user.domain.entity.UserRoleEnum.MASTER;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    // 통합 후 서비스로 교체
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;

    // 리뷰 등록
    @Transactional
    public ReviewCreateResponse createReview(UUID orderId, UUID userId, ReviewCreateRequest request) {
        // 1. 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()->new BaseException(OrderErrorCode.ORDER_NOT_FOUND));

        // 2. 본인 주문 검증
        if (!order.getCustomerId().equals(userId)) {
            throw new BaseException(OrderErrorCode.ORDER_ACCESS_DENIED);
        }

        // 3. 연관 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findById(order.getStoreId())
                .orElseThrow(() -> new BaseException(StoreErrorCode.STORE_NOT_FOUND));

        // 4. 리뷰 엔티티 생성
        Review review = Review.builder()
                .user(user)
                .store(store)
                .order(order)
                .rating(request.getRating())
                .content(request.getContent())
                .build();
        reviewRepository.save(review);

        return ReviewCreateResponse.builder()
                .reviewId(review.getId())
                .orderId(orderId)
                .storeId(store.getId())
                .customerId(userId)
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .createdBy(review.getCreatedBy())
                .build();
    }

    // 리뷰 목록 조회
    @Transactional(readOnly = true)
    public ReviewPageResponse getPagedReviews(UUID userId, UUID targetId, UserRoleEnum role, Pageable pageable) {
        Page<Review> reviews;
        UUID queryUserId = targetId != null ? targetId : userId;

        switch (role) {
            case MASTER, MANAGER -> {
                // targetId로 특정 유저 리뷰 조회
                reviews = reviewRepository.findByUserId(queryUserId, pageable);
            }
            case CUSTOMER -> {
                // 본인 리뷰만 조회 가능
                if (targetId != null && !targetId.equals(userId)) {
                    throw new BaseException(ReviewErrorCode.REVIEW_ACCESS_DENIED);
                }
                reviews = reviewRepository.findByUserId(userId, pageable);
            }
            case OWNER -> {
                // 리뷰 목록 조회 권한 없음
                throw new BaseException(ReviewErrorCode.REVIEW_ACCESS_DENIED);
            }
            default -> throw new BaseException(ReviewErrorCode.REVIEW_ACCESS_DENIED);
        }

        return ReviewPageResponse.from(reviews);
    }

    // 특정 가게 리뷰 목록 조회
    @Transactional(readOnly = true)
    public ReviewPageResponse getStoreReviews(UUID storeId, Pageable pageable) {
        // storeId 유효성 검증
        if (!storeRepository.existsById(storeId)) {
            throw new BaseException(StoreErrorCode.STORE_NOT_FOUND);
        }

        Page<Review> reviews =  reviewRepository.findByStoreId(storeId, pageable);

        return ReviewPageResponse.from(reviews);
    }

    // 리뷰 상세 조회
    @Transactional(readOnly = true)
    public ReviewDetailResponse getDetailReview(UUID reviewId) {
        Review review = reviewRepository.getReviewById(reviewId)
                .orElseThrow(() -> new BaseException(ReviewErrorCode.REVIEW_NOT_FOUND));

        return ReviewDetailResponse.from(review);
    }
}
