## 시스템 흐름

본 문서는 오늘한끼 서비스의 주요 기능 흐름(주문, 결제, 상태 변경, AI 기능)을 정의합니다.

---
### 1. 주문 생성 흐름

```text
CUSTOMER
→ 가게 조회
→ 메뉴 선택
→ 주문 생성 요청

SYSTEM
→ Order 생성 (PENDING)
→ OrderItem 생성 (메뉴 스냅샷 저장)
→ Payment 정보 생성 (CARD)
→ 주문 상태 PENDING 저장
```

---

### 2. 주문 상태 변경 흐름
```text
PENDING(주문요청, CUSTOMER)
    → ACCEPTED(주문수락, OWNER)
        → COOKING(조리완료, OWNER)
            → DELIVERING(배송수령, OWNER)
                → DELIVERED(배송완료, OWNER)
                    → COMPLETED(주문완료, OWNER)
```

### 권한별 규칙

- CUSTOMER: 주문 생성, 5분 이내 취소만 가능
- OWNER: 순서대로 상태 변경만 가능 (역방향 불가)
- MANAGER / MASTER: 모든 상태 변경 가능

---

### 3. 주문 취소 흐름

```text
CUSTOMER 요청
→ 주문 생성 시간 확인

조건 판단

5분 이내 → CANCEL 가능
5분 초과 → 취소 불가

SYSTEM 처리
→ Order 상태 CANCELLED 변경
```
---

### 4. 가게 등록 흐름

```text
OWNER
→ 가게 등록 요청

SYSTEM
→ 카테고리 존재 여부 검증
→ 운영지역 존재 여부 검증
→ OWNER 권한 검증

→ Store 생성
→ 평균 평점 초기화 (0.0)
→ is_hidden = false 설정
→ 데이터 저장
```
---

### 5. 가게 검색 전략 (QueryDSL)

```text
CUSTOMER
→ 가게 검색 요청 (category, name)

SYSTEM
→ QueryDSL 동적 쿼리 생성

조건 분기
category만 입력
→ 해당 카테고리 가게 조회

category + name 입력
→ 카테고리 + 이름 LIKE 검색

→ is_hidden = false 조건 필터링
→ 결과 반환 (평균 평점 포함)
```
---

### 6. 결제 흐름

```text
ORDER
→ 결제 요청

SYSTEM
→ Payment 생성 (status: PENDING)
→ 카드 결제 처리

→ 결제 성공
→ Payment 상태 변경 (PENDING → COMPLETED)

→ 결제 실패
→ Payment 상태 변경 (PENDING → CANCELLED)
```
---

### 7. AI 메뉴 설명 생성 흐름

```text
OWNER
→ 메뉴 등록 요청 (aiDescription = true)

SYSTEM
→ Gemini API 호출
→ 메뉴 설명 생성
→ Menu.description 저장
→ AI Request Log 저장 (request / response 기록)
```
___

### 8. 권한 검증 흐름

```text
HTTP 요청
→ JWT 검증
→ DB에서 사용자 role 재검증
→ 권한 비교

결과
→ 허용 시 API 실행
→ 불일치 시 403 반환
```
---

### 9. 데이터 흐름 요약

- 모든 주문은 상태 기반으로 관리된다
- 삭제는 Soft Delete 방식으로 처리된다
- 주문은 스냅샷 기반으로 과거 데이터 보존