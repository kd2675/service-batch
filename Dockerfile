FROM gradle:8.7-jdk17 as builder
WORKDIR /build
# 그래들 파일이 변경되었을 때만 새롭게 의존패키지 다운로드 받게함.
COPY build.gradle /build/
RUN gradle build -x test --parallel --continue > /dev/null 2>&1 || true
# 빌더 이미지에서 애플리케이션 빌드
COPY . /build
RUN gradle build -x test --parallel
FROM openjdk:17-slim
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