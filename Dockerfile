# OpenJDK 17을 베이스 이미지로 사용
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 래퍼와 설정 파일들을 먼저 복사 (캐싱 최적화)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# ★★★ build 명령어로 테스트와 빌드를 한 번에 실행 ★★★
# 1. test 태스크를 실행하여 모든 테스트를 수행합니다.
# 2. 테스트가 성공하면 bootJar 태스크를 실행하여 JAR 파일을 생성합니다.
# 3. 테스트가 실패하면 전체 build 태스크가 실패하고, Docker 빌드가 중단됩니다.
RUN ./gradlew build --no-daemon

# build/libs 디렉토리 안에 어떤 파일들이 생성되었는지 목록을 출력합니다.
RUN ls -l build/libs

# JAR 파일을 app.jar로 복사
RUN cp build/libs/app.jar app.jar

# 8080 포트 노출
EXPOSE 8080

# 애플리케이션 실행
CMD ["java", "-jar", "app.jar"]