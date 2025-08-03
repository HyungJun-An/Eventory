# 1) Build stage
FROM maven:3.9.11-eclipse-temurin-21 AS builder

WORKDIR /app

# 소스 복사
COPY pom.xml .
COPY src ./src

# 패키징
RUN mvn clean package -DskipTests

# 2) Run stage
FROM openjdk:21-jdk

WORKDIR /app

# 빌드된 jar 복사
COPY --from=builder /app/target/*.jar app.jar

# 포트 설정
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
