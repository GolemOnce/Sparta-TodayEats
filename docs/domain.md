## 도메인

총 11개 도메인으로 구성되어 있습니다.

---

### 1. 도메인 정의

| 도메인 | 설명 | 비고 |
|------|------|------|
| User (사용자) | 회원가입, 로그인, 사용자 관리 | PK: username (유저만 UUID 예외) |
| Area (운영 지역) | 운영 지역 관리 | 초기 광화문, 향후 확장 |
| Category (카테고리) | 음식점 분류 | 한식, 중식, 분식, 치킨, 피자 |
| Store (가게) | 가게 등록 및 관리 | OWNER 소유, 카테고리 + 지역 연결 |
| Menu (메뉴/상품) | 가게별 메뉴 관리 | 가격, 설명, 숨김 처리, AI 설명 생성 |
| Order (주문) | 주문 생성 및 상태 관리 | 5분 이내 취소, 상태 흐름 관리 |
| OrderItem (주문 상품) | 주문에 포함된 개별 메뉴 | 수량, 주문 시점 단가 스냅샷 |
| Review (리뷰) | 주문 완료 후 리뷰 및 평점 | 1~5점, 1주문 1리뷰 |
| Payment (결제) | 주문 결제 정보 | 카드만, PG 미연동 DB 저장 |
| Address (배송지) | 사용자 배송지 관리 | 주소지 필수 |
| AI Request Log (AI 요청 로그) | AI API 요청/응답 기록 | 질문 + 답변 저장 |

---

### 2. 권한 체계

| 역할 | 코드 | 설명 |
|------|------|------|
| 고객 | CUSTOMER | 주문 생성, 본인 주문 조회, 리뷰 작성 |
| 가게 주인 | OWNER | 본인 가게/메뉴/주문 관리, 주문 상태 변경 |
| 서비스 담당자 | MANAGER | 전체 가게 및 주문 관리 권한 |
| 최종 관리자 | MASTER | 전체 권한 + MANAGER 관리 (생성/삭제) |

---

## 주문 상태
### 1. 주문 상태 흐름

| 상태 | 설명 | 변경 주체 |
|------|------|------------|
| PENDING | 주문 요청 상태 | CUSTOMER |
| ACCEPTED | 주문 수락 상태 | OWNER |
| COOKING | 조리 진행 상태 | OWNER |
| DELIVERING | 배달 진행 상태 | OWNER |
| DELIVERED | 배달 완료 상태 | OWNER |
| COMPLETED | 주문 완료 상태 | OWNER |

---

### 2. 상태 변경 규칙

- CUSTOMER는 주문 생성(PENDING) 및 5분 이내 주문 취소만 가능
- OWNER는 상태를 순서대로만 변경 가능 (역방향 변경 불가)
- MANAGER, MASTER는 모든 주문 상태 변경 가능

---

## 도메인 관계

| 관계 | 카디널리티 | 설명 |
|------|------|------|
| User ↔ Store | 1:N | OWNER 한 명이 여러 가게 소유 |
| User ↔ Order | 1:N | CUSTOMER 한 명이 여러 주문 생성 |
| User ↔ Address | 1:N | 사용자별 여러 배송지 |
| User ↔ AI Request Log | 1:N | 사용자별 AI 요청 기록 |
| Area ↔ Store | 1:N | 지역별 여러 가게 |
| Category ↔ Store | 1:N | 카테고리별 여러 가게 |
| Store ↔ Menu | 1:N | 가게별 여러 메뉴 |
| Store ↔ Order | 1:N | 가게별 여러 주문 |
| Store ↔ Review | 1:N | 가게별 여러 리뷰 |
| Order ↔ OrderItem | 1:N | 주문별 여러 주문 상품 |
| Order ↔ Review | 1:1 | 주문당 리뷰 1개 (UNIQUE) |
| Order ↔ Payment | 1:1 | 주문당 결제 1건 (UNIQUE) |
| Menu ↔ OrderItem | 1:N | 메뉴별 여러 주문 상품 |