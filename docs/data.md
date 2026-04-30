## 데이터 설계

### 1. 데이터 설계 원칙
- 모든 테이블은 `p_` 접두사 사용
- 모든 PK는 UUID로 통일
- 모든 데이터는 Soft Delete 방식 적용 (`deleted_at` 사용)
- 공통 Audit Entity를 상속하여 생성/수정/삭제 이력 관리
- 실제 삭제가 아닌 상태 기반 데이터 관리 구조 적용

---

### 2. Soft Delete 정책
- DELETE 쿼리 대신 `deleted_at` 필드 업데이트 방식 사용
- 삭제 데이터는 조회 시 기본적으로 제외 처리
- 데이터 복구 가능하도록 구조 설계

---

### 3. PK 전략
- 모든 엔티티의 Primary Key는 UUID 사용
- 유저(User) 엔티티는 예외적으로 username 기반 PK 사용
- 분산 환경 및 확장성을 고려한 설계

---

### 4. Audit 공통 설계
- 모든 엔티티는 공통 Audit Entity 상속
- 포함 필드:
    - created_at
    - updated_at
    - deleted_at
    - created_by
    - updated_by

---

### 5. 데이터 무결성 및 확장성
- FK 기반 관계 설계로 데이터 정합성 유지
- Soft Delete 기반으로 이력 데이터 보존
- 향후 트래픽 증가 및 서비스 확장을 고려한 구조 설계

---

### 6. ERD (Entity Relationship Diagram)

<div align="center">

<img src="../images/erd.png" width="600"/>

</div>

<div align="center">

<img src="../images/erd.png" width="600"/>

</div>