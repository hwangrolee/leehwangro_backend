# 와이어바알리(wirebarley) 코딩 과제

## 개발환경
- IDE : Intellij Community
- JDK : Amazon Corretto 17 (JAVA 17)
- https://start.spring.io/ 통해서 spring boot 프로젝트 생성

## 로컬에서 실행하기
-- --spring.profiles.active=local 추가하여 실행

## Docker 배포하기

### .env.development 생성 

프로젝트 루트 경로에 .env.development 파일 생성
```bash
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/wirebarley_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=wirebarley
SPRING_DATASOURCE_PASSWORD=4eDWuwwbaHCAL669SDti9OPF
MYSQL_ROOT_PASSWORD=63ujStdG7OfbkOsxcotEam2k
```

### Docker 초기화

기존 컨테이너 완전 정리
```bash
docker-compose --env-file .env.development down -v --remove-orphans
````

이미지까지 삭제 (완전 초기화)
```commandline
docker-compose --env-file .env.development down --rmi all -v --remove-orphans
```

### 빌드 후 실행
```bash
docker-compose --env-file .env.development up --build -d
 ```

## 테스트 코드

### 테스트 코드 실행 방법

프로젝트 루트 경로에서 실행
```commandline
./gradlew test
```

### 유닛 테스트
