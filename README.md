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

## 📊 8. 비관적 / 낙관적 / 분산 락 비교 분석 표 (핵심 평가 규격)

| 비교 항목 | 비관적 락 (Pessimistic Lock) | 낙관적 락 (Optimistic Lock) | 분산 락 (Distributed Lock - Redis Redisson) |
| :--- | :--- | :--- | :--- |
| **관리 주체** | **RDBMS 엔진** (`SELECT ... FOR UPDATE` 배타락 제어) | **애플리케이션** (`@Version` 컬럼 CAS 비교 검증) | **별도 인프라** (Redis 인스턴스 메모리 임계구역 제어) |
| **보호 범위** | **DB 물리 레코드/테이블 단위** (타 트랜잭션 접근 차단) | **DB 논리적 레코드 단위** (커밋 시 버전 불일치 감지) | **분산 서버 간 임계 구역(Critical Section)** 전반 |
| **성능 특성** | DB 단에서 대기하므로 App 재시도 부하 **없음** | 실패 시 App 무한 루프 재시도로 **DB 커넥션 및 CPU 부하 폭증** | Redis 메모리 연산으로 빠르나 네트워크 I/O 비용 발생 |
| **적용 시나리오** | **충돌이 빈번한(High Contention)** 선착순 이벤트 트래픽 | **충돌이 거의 없는(Low Contention)** 일반 정보 수정 | 다중 인스턴스/MSA 환경의 대용량 동기화 제어 |

---

## 🎯 9. 본 프로젝트에서 '비관적 락'을 최종 선택한 아키텍처 근거

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

## 💡 10. 향후 트래픽 확장 시의 아키텍처 발전 로드맵

현재의 DB 비관적 락 방식은 **초당 수백~수천 건 수준의 이벤트 트래픽**까지는 RDBMS의 Connection Pool 한도 내에서 가장 안정적이고 비용 효율적입니다.

단, 서비스가 성장하여 **다중 인스턴스 스케일아웃(Scale-out)**이 이루어지거나 초당 수만 건의 대규모 이벤트가 상시 열릴 경우, DB 커넥션 고갈을 막기 위해 **[Redis Redisson Pub/Sub 분산 락 ➡️ Kafka 기반 비동기 발급 대기열 큐]** 방식으로 비동기 이벤트 아키텍처를 진화시킬 계획입니다.

---

## 🚀 11. 왜 검색 API에 Cache를 적용했는가

### [상품 검색 성능 개선을 위한 Caffeine Local Cache 적용]

#### 배경 및 문제점
상품 검색 API는 사용자가 가장 자주 호출하는 대표적인 조회성 API입니다.  
특히 같은 키워드, 같은 브랜드, 같은 가격 조건으로 검색을 반복하거나 같은 페이지를 다시 조회하는 경우가 많습니다.

별도의 캐시 없이 검색 API를 구현하면, 동일한 요청이 들어오더라도 매번 DB에서 다음 작업이 반복됩니다.

- `LIKE` 조건이 포함된 상품 검색 SQL 실행
- 전체 페이지 수 계산을 위한 count 쿼리 실행
- 동일 조건임에도 매번 DB I/O 발생

이 구조는 데이터가 많아질수록 DB 부하가 누적되고, 검색 요청이 몰릴 경우 응답 속도 저하로 이어질 수 있습니다.

---

## 🧠 12. 캐시 전략 대안 비교 및 선정 이유

### 1. 캐시 미적용 (No Cache) ❌ 비추천
- 구현은 가장 단순하지만, 동일 검색 조건 요청에도 매번 DB 조회가 발생합니다.
- 검색은 읽기 비중이 높은 기능이기 때문에 반복 조회 시 비효율이 큽니다.
- 데이터가 증가할수록 `LIKE` 검색과 count 쿼리 비용이 누적됩니다.

### 2. Write-through / Write-back 전략 ❌ 비추천
- 검색 결과 캐시는 검색 조건 조합이 매우 많아, 쓰기 시점에 캐시를 미리 채우는 방식이 비효율적입니다.
- 상품 등록, 수정, 상태 변경 시 모든 검색 조합을 예측해 캐시를 갱신하는 것은 현실적으로 어렵습니다.
- 검색 결과처럼 조회 중심 데이터에는 과한 전략입니다.

### 3. Cache-aside + Local Cache (Caffeine) 🟢 최종 선택
- 조회 시점에 필요한 데이터만 캐시에 저장하므로 구조가 단순합니다.
- 동일한 검색 조건이 반복될 경우 첫 요청 이후에는 캐시에서 바로 응답할 수 있습니다.
- 검색 API처럼 읽기 비중이 높은 기능에 적합합니다.
- Spring Cache와 Caffeine을 함께 사용하면 구현 난이도도 낮고, TTL과 maximumSize 설정도 간단합니다.

---

## 📊 13. 캐시 적용 전/후 비교 관점

| 비교 항목 | 캐시 미적용 v1 검색 API | Caffeine 캐시 적용 v2 검색 API |
| :--- | :--- | :--- |
| **조회 방식** | 매 요청마다 DB 조회 | 첫 요청만 DB 조회, 이후 캐시 재사용 |
| **SQL 실행 횟수** | 동일 요청에도 매번 실행 | 동일 요청 반복 시 최초 1회만 실행 |
| **응답 속도** | 반복 요청에도 큰 차이 없음 | 반복 요청 시 더 빠른 응답 가능 |
| **DB 부하** | 요청 수만큼 계속 증가 | 동일 조건 요청은 캐시가 흡수 |
| **적합성** | 소규모 / 단순 테스트 수준 | 읽기 비중이 높은 검색 API에 적합 |

---

## 🎯 14. 본 프로젝트에서 Cache-aside + Caffeine을 선택한 근거

### ① 검색 API는 대표적인 읽기 중심(Read-heavy) API
- 상품 검색은 등록 / 수정 / 삭제보다 훨씬 자주 호출됩니다.
- 특히 메인 상품 목록, 브랜드 검색, 키워드 검색, 가격 조건 검색은 반복 조회 빈도가 높습니다.
- 이런 구조에서는 캐시 적용 효율이 높습니다.

### ② 동일 조건의 반복 요청이 많음
- 사용자는 인기 브랜드나 특정 상품명을 반복 조회합니다.
- 같은 검색 결과 페이지를 새로고침하거나 다시 조회하는 경우도 많습니다.
- 동일한 조건에 대해 매번 DB에서 `LIKE` 검색과 count 쿼리를 다시 수행하는 것은 비효율적입니다.

### ③ QueryDSL 동적 검색은 유연하지만 DB 비용이 누적될 수 있음
- 현재 검색 API는 상품명, 브랜드, 가격 범위를 기반으로 동적 쿼리를 구성합니다.
- 최종 SQL에는 `LIKE` 조건과 count 쿼리가 포함됩니다.
- 요청량이 많아질수록 이 비용이 누적되므로, 반복 요청을 캐시로 흡수할 필요가 있습니다.

### ④ 과제 요구사항상 v1 / v2 비교가 중요함
- `v1`은 캐시 미적용 API
- `v2`는 Caffeine Local Cache 적용 API

이렇게 분리함으로써:
- v1은 매번 DB 조회
- v2는 첫 요청 이후 캐시 hit

구조를 비교할 수 있고, 응답 시간과 SQL 실행 횟수 차이를 성능 개선 근거로 제시할 수 있습니다.

### ⑤ 구현 복잡도 대비 효과가 좋음
- Caffeine은 Spring Cache와 쉽게 연동됩니다.
- `@Cacheable` 만으로도 빠르게 도입할 수 있습니다.
- TTL, maximumSize 설정이 쉬워 로컬 환경과 과제 구현에 적합합니다.

---

## ⚠️ 15. 로컬 캐시의 한계와 향후 확장 방향

현재 `v2` 검색 API는 Caffeine 기반 **In-memory Local Cache**를 사용합니다.

### 로컬 캐시의 장점
- 빠른 속도
- 도입이 간단함
- 별도 네트워크 호출 없음

### 로컬 캐시의 한계
- 서버가 여러 대로 늘어나면 캐시가 서버마다 따로 존재합니다.
- 한 서버에서 생성된 캐시가 다른 서버와 공유되지 않습니다.
- 따라서 Scale-out 환경에서는 캐시 불일치 문제가 생길 수 있습니다.

### 향후 확장 방향
서비스가 다중 인스턴스 구조로 확장될 경우:
- `v2` 검색 캐시를 Redis 기반 Remote Cache로 전환
- 서버 간 캐시 공유
- 캐시 일관성 확보

형태로 발전시킬 수 있습니다.

---

## ✅ 16. 정리

기프보따리에서 검색 API에 캐시를 적용한 이유는 다음과 같습니다.

- 상품 검색은 읽기 비중이 높은 대표적인 조회 API이기 때문
- 동일 검색 조건의 반복 요청이 많기 때문
- `LIKE` 기반 검색과 count 쿼리의 DB 부하를 줄이기 위해서
- 캐시 적용 전/후 성능 차이를 비교하기 위해서
- 사용자 체감 응답 속도를 개선하기 위해서

그리고 여러 전략 중에서 **Cache-aside + Caffeine Local Cache**를 선택한 이유는,  
현재 구조에서 가장 단순하면서도 효과적으로 검색 성능을 개선할 수 있는 방법이기 때문입니다.

## 📈 17. 캐시 적용 전/후 성능 비교표

상품 검색 API는 `v1`과 `v2`를 분리하여 캐시 적용 전/후 성능을 비교할 수 있도록 구성했습니다.

- `v1`: 캐시 미적용
- `v2`: Caffeine Local Cache 적용

비교 방법은 같은 검색 조건으로 동일 요청을 여러 번 호출하고,
- 응답 시간
- SQL 실행 여부
  를 함께 확인하는 방식으로 진행했습니다.

### 비교 조건
- 요청 URL
   - `GET /api/v1/products?keyword=스타벅스&page=0&size=10`
   - `GET /api/v2/products?keyword=스타벅스&page=0&size=10`
- 비교 기준
   - 1차 호출
   - 2차 호출
   - 3차 호출
- 관찰 포인트
   - 응답 시간(ms)
   - `gifticon_sale`, `gifticon_product`, `count` SQL 실행 여부

### 성능 비교표 예시

| 구분 | API | 1차 호출 | 2차 호출 | 3차 호출 | 검색 SQL 실행 여부 | 비고 |
| :--- | :--- |:------|:------|:------| :--- | :--- |
| v1 | `/api/v1/products?keyword=스타벅스&page=0&size=10` | 412ms | 29ms  | 28ms  | 매번 실행 | 캐시 미적용 |
| v2 | `/api/v2/products?keyword=스타벅스&page=0&size=10` | 745ms | 19ms  | 20ms  | 1차만 실행 | 이후 cache hit |

### 해석
- `v1`은 같은 요청을 반복해도 매번 DB 검색 SQL과 count 쿼리가 실행됩니다.
- `v2`는 최초 요청 시에만 DB를 조회하고, 이후 동일 조건 요청은 캐시에서 응답합니다.
- 따라서 반복 조회가 많은 검색 API에서는 캐시가 DB 부하를 줄이고 응답 속도를 개선하는 데 효과적입니다.

---

## 🧠 18. 왜 상품 검색 API에 Cache를 적용했는가

상품 검색 API는 사용자 요청 중 조회 비중이 매우 높은 대표적인 Read-heavy API입니다.

### 적용 이유
1. 동일 검색 조건의 반복 요청이 많기 때문
- 사용자는 같은 키워드, 같은 브랜드, 같은 가격 범위로 여러 번 검색할 수 있습니다.
- 같은 결과 페이지를 새로고침하거나 다시 조회하는 경우도 많습니다.

2. QueryDSL 기반 동적 검색의 DB 비용이 누적될 수 있기 때문
- 상품 검색은 `LIKE` 조건과 count 쿼리를 포함합니다.
- 요청량이 많아질수록 DB 부하가 누적됩니다.

3. 캐시 적용 전/후 성능 개선 효과를 명확히 보여줄 수 있기 때문
- `v1`은 캐시 미적용
- `v2`는 캐시 적용
- 동일 조건으로 비교하면 성능 차이를 쉽게 확인할 수 있습니다.

4. 사용자 체감 응답 속도를 개선할 수 있기 때문
- 반복 검색 시 더 빠른 응답을 제공할 수 있습니다.

### 결론
상품 검색 API는 읽기 비중이 높고, 동일 요청 반복 가능성이 높기 때문에 캐시 적용 효과가 큰 대표적인 대상입니다.

---

## 🔥 19. 왜 인기 검색어에도 Cache를 적용했는가

기프보따리에서는 인기 검색어 기능에도 캐시를 적용했습니다.  
인기 검색어는 Redis ZSet을 통해 랭킹을 집계하고, 조회 API 응답은 Caffeine 캐시를 통해 재사용합니다.

즉, 구조를 나누면 다음과 같습니다.

- **집계 저장소**: Redis ZSet
- **조회 응답 캐시**: Caffeine Local Cache

### 인기 검색어에 캐시를 적용한 이유

1. 인기 검색어 조회는 짧은 시간 안에 반복 호출될 가능성이 높기 때문
- 메인 화면 진입
- 검색 페이지 진입
- 사용자 탐색 흐름
  에서 인기 검색어 목록은 자주 조회됩니다.

2. 인기 검색어 결과는 짧은 시간 동안 크게 변하지 않기 때문
- 인기 검색어는 실시간 완전 초단위 변경보다, 짧은 구간 동안 같은 결과가 유지되는 경우가 많습니다.
- 따라서 짧은 TTL의 캐시를 두면 효율이 좋습니다.

3. Redis 조회 자체도 반복 호출 비용이 있기 때문
- Redis는 빠르지만, 조회 요청이 많아질수록 네트워크 I/O와 호출 비용이 계속 발생합니다.
- 자주 조회되는 인기 검색어 응답을 애플리케이션 메모리에서 바로 반환하면 더 빠르게 응답할 수 있습니다.

4. 메인 조회 API 응답 속도를 더 안정적으로 만들 수 있기 때문
- 인기 검색어는 사용자에게 자주 노출되는 UI 요소입니다.
- 캐시를 적용하면 랭킹 조회 응답 속도를 일정하게 유지하는 데 도움이 됩니다.

### 정리
인기 검색어는 Redis ZSet으로 “집계”하고, Caffeine으로 “조회 결과”를 캐싱한 구조입니다.  
즉, Redis는 랭킹 계산용 저장소 역할을 하고, Caffeine은 반복 조회 최적화 역할을 담당합니다.

---

## 🗂 20. 캐시 적용 포인트별 Why 정리

| 적용 대상 | 캐시 종류 | 왜 적용했는가 |
| :--- | :--- | :--- |
| 상품 검색 v2 API | Caffeine Local Cache | 동일 검색 조건 반복 요청이 많고, `LIKE` 검색과 count 쿼리의 DB 부하를 줄이기 위해 |
| 인기 검색어 조회 v2 API | Caffeine Local Cache | 메인/검색 화면에서 반복 조회 가능성이 높고, Redis 조회 결과를 더 빠르게 재사용하기 위해 |
| 인기 검색어 집계 | Redis ZSet | 검색어별 점수 증가와 상위 랭킹 조회를 효율적으로 처리하기 위해 |

---

## ✅ 21. 최종 정리

### 상품 검색 캐시
- 반복 조회가 많음
- DB의 동적 검색 비용이 큼
- 응답 시간 개선 효과가 명확함

### 인기 검색어 캐시
- 짧은 시간 동안 같은 랭킹 결과가 자주 조회됨
- Redis 조회 결과도 반복 최적화 가치가 있음
- 메인 화면 응답 안정성에 도움 됨

즉,
- **상품 검색은 DB 부하 절감**
- **인기 검색어는 Redis 조회 응답 최적화**
  를 목표로 각각 캐시를 적용했습니다.