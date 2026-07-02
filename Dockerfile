# Stage 1: Build the application
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy Maven wrapper and POM
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Set execution permission and cache dependencies
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline

# Copy source code and build
COPY src src
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the final Docker image
FROM eclipse-temurin:17-jre
WORKDIR /app
VOLUME /tmp

# Cập nhật danh sách gói và cài đặt Tesseract OCR cùng dữ liệu tiếng Việt (Dành cho Ubuntu/Debian)
RUN apt-get update && apt-get install -y --no-install-recommends \
    tesseract-ocr \
    tesseract-ocr-vie \
    && rm -rf /var/lib/apt/lists/*

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Copy tessdata for OCR processing
COPY tessdata ./tessdata

ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080
