# 🎁 기프보따리 (Gifbottary) - 기프티콘 거래 플랫폼

기프보따리는 개인 회원 간 중고 기프티콘 거래와 플랫폼 기프티콘 판매를 함께 지원하는 C2B2C 서비스이며,
판매자와 구매자 간 실시간 당근마켓형 채팅을 지원하며, 선착순 프로모션 이벤트를 제공하는 이커머스 백엔드 애플리케이션입니다.

---

## ⚡ 1. 핵심 기능 요약

- **JWT 기반 인증/인가**
   - 회원가입 / 로그인
   - Access Token 기반 인증
   - Spring Security로 공개 API와 인증 API 분리
- **기프티콘 상품/판매글 관리**
   - 개인 판매 상품 등록
   - 플랫폼 판매 상품 등록
   - 상품 원본 정보 / 판매글 / 핀 자산 분리 설계
- **기프티콘 핀 자산 보안 관리**
   - 핀 번호 암호화 저장
   - 해시값(`pin_hash`) 기반 단방향 중복 검증
   - 핀 검수 상태 / 판매 상태 관리
- **구매 / 결제 도메인 분리**
   - 구매 상태와 결제 상태를 독립 관리
   - PortOne 연동을 고려한 결제 확정 구조
- **검색 및 성능 개선**
   - QueryDSL 기반 동적 검색
   - 상품명 / 브랜드 / 가격 범위 검색
   - Redis ZSet 기반 인기 검색어 집계
   - Caffeine 기반 상품 검색 캐시 적용
- **당근마켓형 실시간 웹소켓 메신저**
   - 네트워크 단절 감지 및 읽음 커서 자동 복구
   - STOMP 기반 실시간 채팅
   - 채팅방 / 메시지 영속화
   - 구독 생명주기 분리(`ChatRoomService` / `ChatMemberService`)
- **선착순 할인쿠폰 이벤트**
   - 병목 경합 환경의 동시성 무결성 제어
   - 비관적 락 기반 정원 컷오프 보장

---

## 🛠 2. 기술 스택

### Backend
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- QueryDSL
- WebSocket + STOMP
- Gradle

### Database / Cache / Infra
- MySQL
- Redis
- Caffeine Cache

### Test
- JUnit5
- Mockito
- Spring Boot Test

---

## 📦 3. 패키지 구조

```text
com.example.gifbottary
├─ common
│  ├─ config
│  │  ├─ CacheConfig
│  │  └─ QuerydslConfig
│  ├─ entity
│  │  └─ BaseEntity
│  ├─ exception
│  ├─ response
│  │  └─ ApiResponse
│  ├─ security
│  │  ├─ config
│  │  └─ principal
│  └─ util
│     └─ PinEncryptor
├─ domain
│  ├─ auth
│  │  ├─ controller
│  │  ├─ dto
│  │  ├─ jwt
│  │  └─ service
│  ├─ user
│  │  ├─ entity
│  │  └─ repository
│  ├─ product
│  │  ├─ controller
│  │  ├─ dto
│  │  ├─ entity
│  │  ├─ enums
│  │  ├─ repository
│  │  └─ service
│  ├─ search
│  │  ├─ config
│  │  ├─ controller
│  │  ├─ dto
│  │  ├─ entity
│  │  ├─ repository
│  │  └─ service
│  ├─ purchase
│  │  ├─ controller
│  │  ├─ dto
│  │  ├─ entity
│  │  ├─ enums
│  │  ├─ repository
│  │  └─ service
│  ├─ payment
│  │  ├─ controller
│  │  ├─ dto
│  │  ├─ entity
│  │  ├─ enums
│  │  ├─ repository
│  │  └─ service
│  └─ chat
│     ├─ config
│     ├─ controller
│     ├─ dto
│     ├─ entity
│     ├─ repository
│     └─ service
└─ GifbottaryApplication
```
## 🧩 4. 도메인 설계

### 4.1 인증 / 회원 도메인

#### 역할
- 회원가입과 로그인 처리
- JWT 발급 및 인증 정보 검증
- Spring Security 기반 접근 제어

#### 설계 포인트
- 로그인 성공 시 JWT Access Token 발급
- Security Filter에서 토큰 검증
- `AuthenticationPrincipal` 기반 사용자 식별
- 공개 조회 API와 인증 필요 API 분리

---

### 4.2 상품 / 판매글 / 핀 도메인

#### 핵심 엔티티

**`GifticonProduct`**
- 브랜드
- 상품명
- 권면가
- 이미지 URL 등 상품 원본 정보

**`GifticonSale`**
- 실제 판매글 정보
- 판매 유형
- 판매 상태
- 판매 가격
- 유효기간
- 재고 관리

**`GifticonPin`**
- 개별 핀 자산 정보
- 암호화된 핀 번호
- 핀 해시
- 검수 상태
- 판매 상태 관리

#### 왜 분리했는가
초기에는 판매글에 핀 번호를 직접 두는 구조도 가능했지만, 플랫폼 상품은 동일 상품을 여러 개 보유할 수 있어 핀 단위 자산 관리가 필요했습니다.  
그래서 상품 원본 정보, 판매글, 핀 자산을 분리해 역할을 명확히 나눴습니다.

#### 장점
- 플랫폼 다건 판매 지원
- 핀별 검수 상태 관리 가능
- 핀별 판매 상태 관리 가능
- 판매글 재고와 실제 핀 자산의 정합성 유지

---

### 4.3 구매 도메인

#### 역할
- 구매 생성
- 구매 상태 관리
- 핀 노출 상태 관리

#### 설계 포인트
- 구매와 결제를 별도 도메인으로 분리
- 구매 상태와 핀 상태를 분리
- 개인 판매 / 플랫폼 판매의 구매 흐름을 구분

#### 상태 예시
- `PENDING_PAYMENT`
- `PAID`
- `CONFIRMED`
- `REFUNDED`

#### 핀 상태 예시
- `MASKED`
- `REVEALED`

---

### 4.4 결제 도메인

#### 역할
- 결제 생성 및 상태 관리
- PortOne 식별자 관리
- 구매 도메인과 연결된 결제 흐름 처리

#### 설계 포인트
- 결제 ID와 외부 PG 식별자 분리
- 총 결제 금액 / 포인트 사용 금액 / 카드 결제 금액 관리
- 결제 확정 API 구조 분리
- 구매 성공과 결제 성공을 별도 상태로 관리 가능

---

### 4.5 검색 도메인

#### 역할
- 상품 검색
- 최근 검색어 저장
- 인기 검색어 집계

#### 검색 기능
- 상품명 / 브랜드 / 가격 범위 검색
- QueryDSL 기반 동적 검색
- `LIKE` 조건 검색
- count 쿼리 분리
- `Page` 기반 페이징 조회

#### 검색 API 버전
- `v1`: 캐시 미적용
- `v2`: Caffeine Local Cache 적용

---

### 4.6 채팅 / WebSocket 도메인

#### 역할
- 판매자와 구매자 간 실시간 채팅
- 채팅방 관리
- 메시지 저장 및 조회

#### 설계 포인트
- WebSocket + STOMP 기반 양방향 통신
- 채팅방과 메시지 엔티티 분리
- 메시지 영속화
- 채팅 참여자 관리 분리
- 구독 생명주기 관리 구조 분리

#### 기대 효과
- 상품 문의와 거래 진행의 실시간성 확보
- 채팅 이력 저장 및 조회 가능
- 향후 읽음 처리, 알림 기능 확장 가능

---

## ▶️ 5. 실행 방법

### 1. MySQL 실행
로컬 MySQL을 실행하고 데이터베이스를 준비합니다.

### 2. Redis 실행
인기 검색어 집계 및 Redis 기반 기능 테스트를 위해 Redis를 실행합니다.

### 3. 환경 설정
`application-local.yml` 또는 환경변수를 설정합니다.

예시:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gifticon
    username: root
    password: your-password

  data:
    redis:
      host: localhost
      port: 6379
```
### 4. 애플리케이션 실행

```
./gradlew bootRun
```

### 5. 테스트 실행

```
./gradlew test
```

---
## 📡 14. 주요 API

### 인증
- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/login`

### 상품
- `POST /api/v1/products`
- `GET /api/v1/products`
- `GET /api/v2/products`
- `GET /api/v1/products/{saleId}`
- `GET /api/v1/products/me`
- `PATCH /api/v1/products/{saleId}`
- `PATCH /api/v1/products/{saleId}/status`
- `PATCH /api/v1/products/{saleId}/pins/{pinId}/validation`
- `GET /api/v1/products/{saleId}/pin-validation`
- `DELETE /api/v1/products/{saleId}`

### 검색
- `GET /api/v1/products?keyword=스타벅스&page=0&size=10`
- `GET /api/v2/products?keyword=스타벅스&page=0&size=10`
- `GET /api/v1/search/recent-keywords`
- `GET /api/v1/search/popular-keywords?limit=10`
- `GET /api/v2/search/popular-keywords?limit=10`

### 구매
- `POST /api/v1/purchases/sales/{saleId}`
- `GET /api/v1/purchases/me`
- `GET /api/v1/purchases/{purchaseId}`

### 결제
- `POST /api/v1/payments`
- `POST /api/v1/payments/{portonePaymentId}/confirm`
- `GET /api/v1/payments/{paymentId}`

### 채팅 REST API
- `POST /api/v1/chat-rooms`
- `GET /api/v1/chat-rooms/me`
- `GET /api/v1/chat-rooms/{chatRoomId}/messages`

### WebSocket / STOMP
- WebSocket Endpoint: `/ws`
- Subscribe: `/topic/chat-rooms/{chatRoomId}`
- Publish

---

## 🔒 7. 핵심 기술 트러블슈팅 및 기술 선정 근거

### [선착순 쿠폰 발급 동시성 제어 - 비관적 락(Pessimistic Lock) 최종 채택]

#### 배경 및 문제점
선착순 10장 한정 프로모션 쿠폰 발급 이벤트 진행 시, 0.1초 만에 수백~수천 명의 동시 요청이 몰리는 **High Contention(잦은 트랜잭션 충돌)** 상황이 발생합니다. 별도의 제어 없이 일반 JPA 트랜잭션으로 구현할 경우 레이스 컨디션(Race Condition)으로 인해 한도 정원을 초과하여 발급되는 심각한 비즈니스 에러가 재현되었습니다.

#### 기술 대안 비교 및 선정 이유

1. **낙관적 락 (Optimistic Lock - `@Version` CAS)** ❌ 비추천
   * 경합이 심한 선착순 이벤트에 적용할 경우, 최초 1명만 성공하고 나머지 대기자들은 CAS 실패 예외를 내뿜으며 애플리케이션 단에서 재시도 무한 루프를 돌게 됩니다. 이로 인해 DB Connection Pool 고갈 및 CPU 쓰레드 폭증으로 서버 전체가 장애에 빠지게 됩니다.
2. **분산 락 (Distributed Lock - Redis Redisson)** ❌ 비추천 (현재 아키텍처 기준)
   * 다중 WAS 인스턴스(MSA) 환경에서는 분산 락이 필수적이지만, 현재의 단일 모놀리식 서버 인스턴스 구조에서 선착순 수량 하나를 동기화하기 위해 별도의 Redis 인프라를 전용 구축하는 것은 **오버엔지니어링이자 운영 유지비용 낭비**입니다.
3. **비관적 락 (Pessimistic Lock - `SELECT ... FOR UPDATE`)** 🟢 **최종 선택**
   * 트랜잭션 쓰레드들이 애플리케이션 단이 아닌 DB 엔진 단에서 줄을 서서 대기하므로 재시도 부하가 원천 차단됩니다.
   * 선착순 10장 번째 커밋이 완료되는 즉시 11번 번째 대기자는 정확히 수량 부족 예외를 받고 탈락 처리되어 **정원 컷오프 무결성이 100% 보장**됩니다.

---

## 📊 3. 비관적 / 낙관적 / 분산 락 비교 분석 표 (핵심 평가 규격)

| 비교 항목 | 비관적 락 (Pessimistic Lock) | 낙관적 락 (Optimistic Lock) | 분산 락 (Distributed Lock - Redis Redisson) |
| :--- | :--- | :--- | :--- |
| **관리 주체** | **RDBMS 엔진** (`SELECT ... FOR UPDATE` 배타락 제어) | **애플리케이션** (`@Version` 컬럼 CAS 비교 검증) | **별도 인프라** (Redis 인스턴스 메모리 임계구역 제어) |
| **보호 범위** | **DB 물리 레코드/테이블 단위** (타 트랜잭션 접근 차단) | **DB 논리적 레코드 단위** (커밋 시 버전 불일치 감지) | **분산 서버 간 임계 구역(Critical Section)** 전반 |
| **성능 특성** | DB 단에서 대기하므로 App 재시도 부하 **없음** | 실패 시 App 무한 루프 재시도로 **DB 커넥션 및 CPU 부하 폭증** | Redis 메모리 연산으로 빠르나 네트워크 I/O 비용 발생 |
| **적용 시나리오** | **충돌이 빈번한(High Contention)** 선착순 이벤트 트래픽 | **충돌이 거의 없는(Low Contention)** 일반 정보 수정 | 다중 인스턴스/MSA 환경의 대용량 동기화 제어 |

---

## 🎯 4. 본 프로젝트에서 '비관적 락'을 최종 선택한 아키텍처 근거

### ① 선착순 이벤트의 'High Contention(잦은 충돌)' 트래픽 구조
* 선착순 100장 한정 쿠폰에 10,000명이 동시 접속할 경우, **충돌 확률은 99%에 육박**합니다.
* 만약 **낙관적 락(CAS)**을 적용할 경우, 최초 1명만 성공하고 나머지 9,999명은 `OptimisticLockingFailureException`을 내뿜으며 애플리케이션 레벨에서 재시도 루프를 돕니다. 이 과정에서 **수만 번의 헛된 DB Connection 풀 점유와 트랜잭션 롤백 부하가 발생하여 전체 서버가 장애에 빠지게 됩니다.**
* 반면 **비관적 락**은 쓰레드들이 DB 엔진 단에서 줄을 서서 대기하므로 불필요한 애플리케이션 재시도 부하가 원천 차단됩니다.

### ② 단일 인스턴스(Monolith) 환경에서의 인프라 효율성 극대화
* 현재 서비스 아키텍처는 단일 Spring Boot 인스턴스와 RDBMS로 구성되어 있습니다.
* **Redis 분산 락**은 분산 DB 환경이나 다중 WAS 인스턴스 간의 동시성 동기화에 필수적이지만, 단일 DB 환경에서 선착순 수량 하나를 제어하기 위해 별도의 Redis 인프라를 전용 구축하는 것은 **오버엔지니어링(Over-Engineering)이자 운영 비용 낭비**입니다.

### ③ 정확한 정원 컷오프(Cut-off) 무결성 단언
* 비관적 배타락은 `FOR UPDATE`를 통해 조회 시점부터 수정 완료 시점까지 다른 트랜잭션의 읽기/쓰기를 완벽히 잠금 처리합니다. 따라서 정확히 10장 번째 트랜잭션이 커밋되는 순간 11번 번째 대기자는 즉시 `COUPON_OUT_OF_STOCK` 예외를 뱉고 안전하게 탈락 처리됩니다.

---

## 💡 5. 향후 트래픽 확장 시의 아키텍처 발전 로드맵

현재의 DB 비관적 락 방식은 **초당 수백~수천 건 수준의 이벤트 트래픽**까지는 RDBMS의 Connection Pool 한도 내에서 가장 안정적이고 비용 효율적인 정답입니다.

단, 서비스가 성장하여 **다중 인스턴스 스케일아웃(Scale-out)**이 이루어지거나 초당 수만 건의 대규모 이벤트가 상시 열릴 경우, DB 커넥션 고갈을 막기 위해 **[Redis Redisson Pub/Sub 분산 락 ➡️ Kafka 기반 비동기 발급 대기열 큐]** 방식으로 비동기 이벤트 아키텍처를 진화시킬 계획입니다.
