# 🚀 Service-Batch
Second 프로젝트의 배치 작업 실행을 위한 REST API 서비스. 
server-batch와 연동하여 배치 작업의 실행, 모니터링, 관리 기능을 제공.

## 📖 프로젝트 개요
Service-Batch는 Spring Boot 기반의 배치 서비스 API 게이트웨이.
다양한 배치 작업들을 REST API를 통해 실행하고 관리할 수 있는 중간 계층 역할을 수행.

## 🎯 주요 기능
- **배치 작업 API**: REST API를 통한 배치 작업 실행 및 제어

- **Gateway 연동**: server-cloud를 통한 요청 라우팅
- **배치 모니터링**: 배치 작업 상태 확인 및 결과 조회
- **에러 처리**: 배치 실행 중 발생하는 예외 상황 처리
- **로깅**: 배치 작업 실행 이력 및 로그 관리

## 🛠️ 기술 스택
- **Spring Boot 3.2.4**: 메인 애플리케이션 프레임워크
- **Spring Batch**: 배치 처리 프레임워크
- **Spring Data JPA**: 데이터 액세스 계층
- **Spring Web**: RESTful 웹 서비스
- **Spring Validation**: 데이터 유효성 검증

### 데이터베이스
- **MySQL**: 메인 데이터베이스 (mysql-connector-j)

### 웹 크롤링 & 데이터 수집
- **Jsoup 1.15.3**: HTML 파싱 및 웹 스크래핑
- **Selenium Java 3.141.59**: 동적 웹 페이지 크롤링
- **Apache HttpClient5 5.2.1**: HTTP 통신

## 🚀 주요 기능
이 서비스는 다음과 같은 배치 처리 작업을 수행합니다:

1. **웹 크롤링**: Jsoup과 Selenium을 활용한 웹 데이터 수집
2. **데이터 처리**: 수집된 데이터의 변환 및 정제
3. **배치 작업**: Spring Batch를 통한 대용량 데이터 처리
4. **데이터 저장**: JPA를 통한 MySQL 데이터베이스 저장

---

## 🔧 설정 및 실행
- server-batch 프로젝트는 반드시 "second" 프로젝트 디렉터리 내부에 위치해야 합니다.
- 예: .../second/server-batch

이 규칙을 지키지 않으면 빌드/실행 및 배포 스크립트가 실패하도록 구성될 수 있습니다.

### 사전 요구사항
- **JDK 17** 이상
- **MySQL 8.0** 이상
- **Gradle 8.x**