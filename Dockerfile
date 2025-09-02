FROM gradle:8.7-jdk17 as builder
WORKDIR /build

# 🔥 더 세밀한 의존성 캐싱 (서브프로젝트별)
COPY build.gradle settings.gradle /build/
COPY common-core/build.gradle /build/common-core/
COPY common-database/build.gradle /build/common-database/
COPY common-log/build.gradle /build/common-log/
COPY service-batch/build.gradle /build/service-batch/
RUN gradle :service-batch:dependencies --no-daemon

# 🎯 필요한 소스만 복사 (전체 대신)
COPY common-core/ /build/common-core/
COPY common-database/ /build/common-database/
COPY common-log/ /build/common-log/
COPY service-batch/ /build/service-batch/

# 빌드 (기존과 동일)
RUN gradle :service-batch:clean :service-batch:build --no-daemon --parallel

FROM openjdk:17-slim
WORKDIR /app

# 🔧 시스템 패키지 업데이트 및 필수 도구 설치
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# 🌐 Chrome 및 ChromeDriver 설정
ENV CHROME_VERSION=114.0.5735.90-1
ENV CHROME_DRIVER_VERSION=114.0.5735.90

# 🔥 Chrome 설치 (최적화)
RUN wget -q https://mirror.cs.uchicago.edu/google-chrome/pool/main/g/google-chrome-stable/google-chrome-stable_${CHROME_VERSION}_amd64.deb \
    && apt-get install -y ./google-chrome-stable_${CHROME_VERSION}_amd64.deb \
    && rm -f google-chrome-stable_${CHROME_VERSION}_amd64.deb

# 🔥 ChromeDriver 설치 (최적화)
RUN wget -q https://chromedriver.storage.googleapis.com/${CHROME_DRIVER_VERSION}/chromedriver_linux64.zip \
    && unzip -q chromedriver_linux64.zip \
    && mv chromedriver /usr/local/bin/ \
    && chmod +x /usr/local/bin/chromedriver \
    && rm -f chromedriver_linux64.zip

COPY --from=builder /build/service-batch/build/libs/*.jar ./app.jar
ENV	USE_PROFILE dev

ENTRYPOINT ["java", "-Dspring.profiles.active=${USE_PROFILE}", "-jar", "/app/app.jar"]