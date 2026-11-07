
# Giai doan 1: Build ung dung Spring Boot
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Tai va cache cac dependency Maven
COPY pom.xml ./
RUN mvn dependency:go-offline -B || true

# Sao chep ma nguon va build file JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Giai doan 2: Chay ung dung
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Cai wget de kiem tra API health cua Backend
RUN apk add --no-cache wget

# Tao user khong co quyen root de tang bao mat
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Sao chep file JAR tu giai doan build
COPY --from=build /app/target/*.jar app.jar

# Chay ung dung bang user khong co quyen root
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
