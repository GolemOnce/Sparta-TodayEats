package com.sparta.todayeats.global.init;

import com.sparta.todayeats.address.entity.Address;
import com.sparta.todayeats.address.repository.AddressRepository;
import com.sparta.todayeats.area.entity.Area;
import com.sparta.todayeats.area.repository.AreaRepository;
import com.sparta.todayeats.category.entity.Category;
import com.sparta.todayeats.category.repository.CategoryRepository;
import com.sparta.todayeats.menu.entity.Menu;
import com.sparta.todayeats.menu.repository.MenuRepository;
import com.sparta.todayeats.order.entity.Order;
import com.sparta.todayeats.order.entity.OrderStatus;
import com.sparta.todayeats.order.entity.OrderType;
import com.sparta.todayeats.order.repository.OrderRepository;
import com.sparta.todayeats.review.entity.Review;
import com.sparta.todayeats.review.repository.ReviewRepository;
import com.sparta.todayeats.store.entity.Store;
import com.sparta.todayeats.store.repository.StoreRepository;
import com.sparta.todayeats.user.entity.User;
import com.sparta.todayeats.user.entity.UserRoleEnum;
import com.sparta.todayeats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final AreaRepository areaRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (areaRepository.existsByNameIgnoreCaseAndDeletedAtIsNull("광화문")) return; // 이미 있으면 스킵

        // 지역 생성
        Area area = areaRepository.save(Area.builder()
                .name("광화문")
                .city("서울특별시")
                .district("종로구")
                .isActive(true)
                .build());

        // 카테고리 생성
        Category category = createCategory("한식");

        // MASTER 계정 생성
        createUser("master@test.com", "Master123!", "마스터", UserRoleEnum.MASTER);

        // OWNER 계정 생성 - 직접 삭제 시도
        User owner1 = createUser("owner@test.com", "Owner123!", "삭제", UserRoleEnum.OWNER);
        User owner2 = createUser("owner2@test.com", "Owner456!", "삭제안함", UserRoleEnum.OWNER);

        // CUSTOMER 계정 생성 - 관리자 삭제 시도
        User customer1 = createUser("customer@test.com", "User123!", "삭제", UserRoleEnum.CUSTOMER);
        User customer2 = createUser("customer2@test.com", "User456!", "삭제안함", UserRoleEnum.CUSTOMER);

        // 가게 생성
        Store store1 = createStore(owner1, area, category, "삭제의 쌈밥집", "대한민국", "02-123-4567");
        Store store2 = createStore(owner2, area, category, "삭제안함의 쌈밥집", "대한민국", "02-123-4567");

        // 메뉴 생성
        createMenu(store1, category, "제육 쌈밥 정식", 12000L, "매콤한 제육볶음과 신선한 쌈채소");

        // 삭제할 사용자 배송지 등록
        Address address = createAddress(customer1, "우리집", "대한민국", "1호", "12345");

        // 주문 생성
        Order order = createOrder(customer1, store1, address, OrderStatus.COMPLETED);
        createOrder(customer2, store2, address, OrderStatus.PENDING);

        // 리뷰 생성
        createReview(customer1, store1, order, 4, "배달이 빨라서 좋았습니다.");
    }

    private User createUser(String email, String rawPassword, String nickname, UserRoleEnum role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .nickname(nickname)
                    .role(role)
                    .build();
            return userRepository.save(user);
        });
    }

    private Address createAddress(User user, String alias, String addr, String detail, String zip) {
        Address address = Address.builder()
                .user(user)
                .alias(alias)
                .address(addr)
                .detail(detail)
                .zipCode(zip)
                .isDefault(false)
                .build();
        return addressRepository.save(address);
    }

    private Category createCategory(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .build()));
    }

    private Store createStore(User owner, Area area, Category category, String name, String addr, String phone) {
        if (storeRepository.existsByName(name)) {
            return storeRepository.findByName(name).orElse(null);
        }

        Store store = Store.builder()
                .owner(owner)
                .area(area)
                .category(category)
                .name(name)
                .address(addr)
                .phone(phone) // 필요 시 normalizePhone 로직 적용
                .build();
        return storeRepository.save(store);
    }

    private Order createOrder(User user, Store store, Address address, OrderStatus targetStatus) {
        Order order = Order.builder()
                .customerId(user.getUserId())
                .storeId(store.getId())
                .addressId(address.getId())
                .storeName(store.getName())
                .deliveryAddress(address.getAddress())
                .deliveryDetail(address.getDetail())
                .orderType(OrderType.ONLINE)
                .note("문 앞에 놓아주세요.")
                .totalPrice(25000L)
                .build();

        // 주문 완료 상태로 변경
        if (targetStatus == OrderStatus.COMPLETED) {
            order.updateStatus(OrderStatus.ACCEPTED);
            order.updateStatus(OrderStatus.COOKING);
            order.updateStatus(OrderStatus.DELIVERING);
            order.updateStatus(OrderStatus.DELIVERED);
            order.updateStatus(OrderStatus.COMPLETED);
        }

        return orderRepository.save(order);
    }

    private void createReview(User user, Store store, Order order, Integer rating, String content) {
        Review review = Review.builder()
                .user(user)
                .store(store)
                .order(order)
                .rating(rating)
                .content(content)
                .build();
        reviewRepository.save(review);
    }

    private void createMenu(Store store, Category category, String name, Long price, String desc) {
        if (menuRepository.existsByNameAndStore(name, store)) return;

        Menu menu = Menu.builder()
                .name(name)
                .price(price)
                .description(desc)
                .imageUrl("https://example.com/menu-image.png")
                .category(category)
                .store(store)
                .isHidden(false)
                .soldOut(false)
                .build();
        menuRepository.save(menu);
    }
}
