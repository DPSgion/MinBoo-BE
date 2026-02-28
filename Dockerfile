# ==========================================
# Giai đoạn 1: Build ứng dụng (Builder Stage)
# ==========================================
FROM eclipse-temurin:25-jdk-alpine AS builder

# Đặt thư mục làm việc trong container
WORKDIR /app

# Copy các file cấu hình Maven vào trước để tận dụng cache của Docker
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Cấp quyền thực thi cho file mvnw (đề phòng lỗi permission trên Linux/Mac)
RUN chmod +x ./mvnw

# Tải các dependency về trước (nếu pom.xml không đổi, bước này sẽ được cache)
RUN ./mvnw dependency:go-offline

# Copy toàn bộ source code vào container
COPY src ./src

# Build ra file JAR và bỏ qua bước chạy test để tiết kiệm thời gian
RUN ./mvnw clean package -DskipTests

# ==========================================
# Giai đoạn 2: Chạy ứng dụng (Runtime Stage)
# ==========================================
# Ở bước này chỉ cần JRE (Java Runtime Environment) để chạy, không cần JDK để build nữa
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Copy file JAR đã được build từ giai đoạn 'builder' sang giai đoạn này
COPY --from=builder /app/target/*.jar app.jar

# Mở port 8080 (Port mặc định của Spring Boot)
EXPOSE 8080

# Lệnh để khởi chạy ứng dụng khi container bắt đầu
ENTRYPOINT ["java", "-jar", "app.jar"]