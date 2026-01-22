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

FROM openjdk:17-jdk-slim
WORKDIR /app

#RUN apt -y install curl

RUN apt-get update

RUN apt-get install -y curl

RUN apt-get install -y wget

RUN apt-get install -y unzip

ENV	CHROME_VERSION 114.0.5735.90-1
ENV	CHROME_DRIVER_VERSION 114.0.5735.90

#RUN wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb

#RUN apt-get -y install ./google-chrome-stable_current_amd64.deb

RUN wget https://mirror.cs.uchicago.edu/google-chrome/pool/main/g/google-chrome-stable/google-chrome-stable_${CHROME_VERSION}_amd64.deb
RUN apt-get install -y ./google-chrome-stable_${CHROME_VERSION}_amd64.deb

RUN wget https://chromedriver.storage.googleapis.com/${CHROME_DRIVER_VERSION}/chromedriver_linux64.zip
RUN unzip chromedriver_linux64.zip

#RUN wget -O /tmp/chromedriver.zip https://chromedriver.storage.googleapis.com/` curl -sS chromedriver.storage.googleapis.com/${CHROME_DRIVER_VERSION}`/chromedriver_linux64.zip
#RUN unzip /tmp/chromedriver.zip chromedriver -d /usr/bin

COPY --from=builder /build/service-batch/build/libs/*.jar ./app.jar
ENV	USE_PROFILE dev

ENTRYPOINT ["java", "-Dspring.profiles.active=${USE_PROFILE}", "-jar", "/app/app.jar"]