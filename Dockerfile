
# Importing JDK and copying required files
FROM openjdk:17-jdk AS build
WORKDIR /demo
COPY pom.xml .
COPY src src

# Copy Maven wrapper
COPY mvnw .
COPY .mvn .mvn

# Set execution permission for the Maven wrapper
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the final Docker image using OpenJDK 19
FROM openjdk:17-jdk
VOLUME /tmp

RUN apt-get update && \
    apt-get install -y tesseract-ocr && \
    apt-get install -y tesseract-ocr-vie && \
    rm -rf /var/lib/apt/lists/*

# Copy the JAR from the build stage
COPY --from=build /demo/target/*.jar demo.jar
ENTRYPOINT ["java","-jar","/demo.jar"]
EXPOSE 8080

