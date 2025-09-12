# 🚀 Service-Batch
배치 처리 전용 마이크로서비스

## 📖 프로젝트 개요
배치 작업을 전담하는 마이크로서비스
대용량 데이터 처리, 스케줄링된 작업, 크롤링 등의 배치 업무를 안정적으로 수행

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


### 사전 요구사항
- **JDK 17** 이상
- **MySQL 8.0** 이상
- **Gradle 8.x**