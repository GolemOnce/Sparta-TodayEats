package com.sparta.todayeats.review.service;

import com.sparta.todayeats.global.exception.BaseException;
import com.sparta.todayeats.global.exception.OrderErrorCode;
import com.sparta.todayeats.global.exception.StoreErrorCode;
import com.sparta.todayeats.global.exception.UserErrorCode;
import com.sparta.todayeats.order.Repository.OrderRepository;
import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.review.dto.request.ReviewCreateRequest;
import com.sparta.todayeats.review.dto.response.ReviewCreateResponse;
import com.sparta.todayeats.review.entity.Review;
import com.sparta.todayeats.review.repository.ReviewRepository;
import com.sparta.todayeats.store.entity.Store;
import com.sparta.todayeats.store.repository.StoreRepository;
import com.sparta.todayeats.user.domain.entity.User;
import com.sparta.todayeats.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    ReviewRepository reviewRepository;

    // 통합 후 서비스로 교체
    OrderRepository orderRepository;


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

    //
}
