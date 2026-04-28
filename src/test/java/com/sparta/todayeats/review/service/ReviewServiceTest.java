package com.sparta.todayeats.review.service;

import com.sparta.todayeats.global.exception.BaseException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private ReviewService reviewService;

    private UUID userId;
    private UUID orderId;
    private UUID storeId;

    private Order buildOrder(UUID customerId, UUID storeId) {
        return Order.builder()
                .customerId(customerId)
                .storeId(storeId)
                .build();
    }

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        storeId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("리뷰 등록")
    class createReview {

        @Test
        void 주문이_존재하지_않으면_예외() {
            // given
            given(orderRepository.findById(orderId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(orderId, userId, new ReviewCreateRequest(5, "맛있어요")))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        void 본인_주문이_아니면_예외() {
            // given
            UUID otherUserId = UUID.randomUUID();
            Order order = buildOrder(userId, storeId);
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(orderId, otherUserId, new ReviewCreateRequest(5, "맛있어요")))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        void 정상_리뷰_등록_성공() {
            // given
            Order order = buildOrder(userId, storeId);
            User user = mock(User.class);
            Store store = mock(Store.class);

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
            given(reviewRepository.save(any(Review.class))).willAnswer(i -> i.getArgument(0));  // 핵심

            // when
            ReviewCreateResponse response = reviewService.createReview(orderId, userId, new ReviewCreateRequest(5, "맛있어요"));

            // then
            assertThat(response.getRating()).isEqualTo(5);
            assertThat(response.getContent()).isEqualTo("맛있어요");
            verify(reviewRepository).save(any(Review.class));
        }
    }

    @Nested
    @DisplayName("리뷰 목록 조회")
    class getPagedReviews {

        @Test
        void CUSTOMER_본인_리뷰_조회_성공() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Order order = buildOrder(userId, storeId);
            Review review = Review.builder()
                    .user(mock(User.class))
                    .store(mock(Store.class))
                    .order(order)
                    .rating(5)
                    .content("맛있어요")
                    .build();

            Page<Review> reviewPage = new PageImpl<>(List.of(review), pageable, 1);
            given(reviewRepository.findByUserId(userId, pageable)).willReturn(reviewPage);

            // when
            ReviewPageResponse response = reviewService.getPagedReviews(userId, null, UserRoleEnum.CUSTOMER, pageable);

            // then
            assertThat(response.getReviews()).hasSize(1);
            verify(reviewRepository).findByUserId(userId, pageable);
        }

        @Test
        void CUSTOMER_타인_리뷰_조회_시도시_예외() {
            // given
            UUID otherUserId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);

            // when & then
            assertThatThrownBy(() -> reviewService.getPagedReviews(userId, otherUserId, UserRoleEnum.CUSTOMER, pageable))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        void MANAGER_특정_유저_리뷰_조회_성공() {
            // given
            UUID targetId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            Page<Review> reviewPage = new PageImpl<>(List.of(), pageable, 0);
            given(reviewRepository.findByUserId(targetId, pageable)).willReturn(reviewPage);

            // when
            ReviewPageResponse response = reviewService.getPagedReviews(userId, targetId, UserRoleEnum.MANAGER, pageable);

            // then
            assertThat(response).isNotNull();
            verify(reviewRepository).findByUserId(targetId, pageable);
        }

        @Test
        void OWNER_리뷰_목록_조회_시도시_예외() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            // when & then
            assertThatThrownBy(() -> reviewService.getPagedReviews(userId, null, UserRoleEnum.OWNER, pageable))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        void 리뷰_없으면_빈_페이지_반환() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            given(reviewRepository.findByUserId(userId, pageable)).willReturn(Page.empty(pageable));

            // when
            ReviewPageResponse response = reviewService.getPagedReviews(userId, null, UserRoleEnum.CUSTOMER, pageable);

            // then
            assertThat(response.getReviews()).isEmpty();
        }
    }

    @Nested
    @DisplayName("가게 리뷰 목록 조회")
    class getStoreReviews {

        @Test
        void 가게_리뷰_조회_성공() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Review> reviewPage = new PageImpl<>(List.of(), pageable, 0);
            given(storeRepository.existsById(storeId)).willReturn(true);
            given(reviewRepository.findByStoreId(storeId, pageable)).willReturn(reviewPage);

            // when
            ReviewPageResponse response = reviewService.getStoreReviews(storeId, pageable);

            // then
            assertThat(response).isNotNull();
            verify(reviewRepository).findByStoreId(storeId, pageable);
        }

        @Test
        void 가게가_존재하지_않으면_예외() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            given(storeRepository.existsById(storeId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> reviewService.getStoreReviews(storeId, pageable))
                    .isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("리뷰 상세 조회")
    class getDetailReview {

        @Test
        void 리뷰_상세_조회_성공() {
            // given
            UUID reviewId = UUID.randomUUID();

            // Order도 mock으로
            Order order = mock(Order.class);
            User user = mock(User.class);
            Store store = mock(Store.class);

            given(user.getUserId()).willReturn(userId);
            given(store.getId()).willReturn(storeId);
            given(order.getOrderId()).willReturn(orderId);

            Review review = Review.builder()
                    .user(user)
                    .store(store)
                    .order(order)
                    .rating(5)
                    .content("맛있어요")
                    .build();

            given(reviewRepository.getReviewById(reviewId)).willReturn(Optional.of(review));

            // when
            ReviewDetailResponse response = reviewService.getDetailReview(reviewId);

            // then
            assertThat(response.getRating()).isEqualTo(5);
            assertThat(response.getContent()).isEqualTo("맛있어요");
            assertThat(response.getStoreId()).isEqualTo(storeId);
            assertThat(response.getCustomerId()).isEqualTo(userId);
            verify(reviewRepository).getReviewById(reviewId);
        }

        @Test
        void 존재하지_않는_리뷰_조회시_예외() {
            // given
            UUID reviewId = UUID.randomUUID();
            given(reviewRepository.getReviewById(reviewId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.getDetailReview(reviewId))
                    .isInstanceOf(BaseException.class);
        }
    }
}