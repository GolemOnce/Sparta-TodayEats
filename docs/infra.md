## 인프라 & 배포

---

### 1. 시스템 아키텍처
```
   [ GitHub Repository ]
   |
   | push to main
   ▼
   [ GitHub Actions ]
   ├─ 테스트 (./gradlew test)
   ├─ 빌드 (./gradlew bootJar)
   └─ EC2 배포 (SSH)
   |
   ▼
   [ AWS EC2 (Ubuntu) ]
   ├─ Spring Boot  :8080
   ├─ PostgreSQL   :5432
   └─ Redis        :6379
```

---

### 2. 환경 구성
#### 로컬 개발 환경
>PostgreSQL, Redis는 Docker로 로컬에 띄우고 Spring은 IntelliJ에서 직접 실행.
- 실행 방법
```bash
# 1. DB/Redis 컨테이너 실행
docker-compose -f docker-compose.local.yml --env-file .env.local up -d

# 2. IntelliJ에서 Spring 실행
# Run Configuration → Active profiles: local
```

- 필요 파일 (git 미포함 → 직접 생성 필요)
  - application-local.yml : 로컬 DB/Redis 연결 정보
  - .env.local : Docker Compose 환경변수
<br></br>
#### 운영 환경 (EC2)
>모든 서비스(Spring, PostgreSQL, Redis)를 Docker Compose로 EC2에서 실행.
- 실행 방법
```bash
docker-compose up -d --build
```
- 필요 파일 (git 미포함 → EC2에 직접 생성 필요)
  - .env : 운영 Docker Compose 환경변수

### 4. 환경변수 목록
```
# Postgres
POSTGRES_DB=
POSTGRES_USER=
POSTGRES_PASSWORD=

// .env 전용
# Spring Datasource
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=

# Redis
REDIS_PASSWORD=
```

---

### 5. CI/CD 파이프라인
- 트리거 
  - main, develop(CI) 브랜치에 push(머지) 시 자동 실행
<br></br>
- 파이프라인 흐름
```
1. 코드 체크아웃
2. JDK 17 세팅
3. 테스트 실행 (실패 시 중단)
(develop은 여기서 종료)
4. bootJar 빌드
5. EC2 SSH 접속
6. git pull
7. docker-compose down → up --build
```

- GitHub Secrets 설정
  - `EC2_HOST` : EC2 퍼블릭 IP
  - `EC2_USERNAME` : EC2 접속 유저명 (ubuntu)
  - `EC2_SSH_KEY` : .pem 파일 전체 내용
  - `SPRING_DATASOURCE_URL` : 운영 DB URL
  - `SPRING_DATASOURCE_USERNAME` : 운영 DB 유저명
  - `SPRING_DATASOURCE_PASSWORD` : 운영 DB 비밀번호
  - `REDIS_PASSWORD` : Redis 비밀번호
---
### 6. 브랜치 전략
```
main        ← 운영 배포 브랜치 (CI/CD 트리거)
develop     ← 개발 통합 브랜치 (CI 트리거)
feat/#이슈번호-기능명  ← 기능 개발 브랜치
```
- 개발 흐름
```
feat 브랜치 생성 → 개발 → PR to develop → 코드 리뷰(CodeRabbit) → merge 
→ merge 시 CI → develop → main PR → merge 시 CI/CD 자동 배포
```
---
### 7. 로컬 개발 환경 세팅 가이드
- 처음 세팅하는 팀원을 위한 가이드.
1. 레포 클론
```bash
git clone https://github.com/GolemOnce/Sparta-TodayEats.git
cd Sparta-TodayEats
```
2. 환경 파일 생성
- 루트 디렉토리에 .env.local 생성 후 값 채우기.
- src/main/resources/application-local.yml 생성 후 값 채우기.
3. 로컬 DB/Redis 실행
```
docker-compose -f docker-compose.local.yml --env-file .env.local up -d
```
4. Spring 실행
- IntelliJ → 상단 Run탭 → Run Configuration → Active profiles: local 설정 → 실행(Run)