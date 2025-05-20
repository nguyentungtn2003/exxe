# Use OpenJDK 17 slim image làm base
FROM openjdk:17-jdk-slim

# Tạo thư mục /app trong container và set làm working directory
WORKDIR /app

# Copy file jar đã build vào container (giả sử file jar nằm trong thư mục target)
COPY target/*.jar app.jar

# Mở port 8080 (port mặc định của Spring Boot)
EXPOSE 8080

# Chạy file jar khi container khởi động
ENTRYPOINT ["java", "-jar", "app.jar"]
