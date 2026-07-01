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
## 📡 6. 주요 API

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

## 🔒 7. 동시성 제어 - 비관적 / 낙관적 / 분산 락 비교 분석 및 선택 근거
### [Wiki - 💡선착순 쿠폰 발급 시나리오 ‐ 동시성 제어](https://github.com/sparta-exit6/gifbottary/wiki/%F0%9F%92%A1-%EC%84%A0%EC%B0%A9%EC%88%9C-%EC%BF%A0%ED%8F%B0-%EB%B0%9C%EA%B8%89-%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4-%E2%80%90-%EB%8F%99%EC%8B%9C%EC%84%B1-%EC%A0%9C%EC%96%B4)

---
## 🚀 8. 캐싱을 이용한 성능 개선
### [Wiki - 💡캐싱을 이용한 성능 개선 ‐ 상품 검색 인기 검색어 조회](https://github.com/sparta-exit6/gifbottary/wiki/%F0%9F%92%A1-%EC%BA%90%EC%8B%B1%EC%9D%84-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0-%E2%80%90-%EC%83%81%ED%92%88-%EA%B2%80%EC%83%89-%EC%9D%B8%EA%B8%B0-%EA%B2%80%EC%83%89%EC%96%B4-%EC%A1%B0%ED%9A%8C)

---
## 🔖 9. 인덱싱 최적화
### [Wiki - 💡 최적화 ‐ 인덱싱](https://github.com/sparta-exit6/gifbottary/wiki/%F0%9F%92%A1-%EC%B5%9C%EC%A0%81%ED%99%94-%E2%80%90-%EC%9D%B8%EB%8D%B1%EC%8B%B1)