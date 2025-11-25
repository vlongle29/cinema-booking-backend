## Giai đoạn 1: Build ứng dụng từ mã nguồn
## Sử dụng một image Java có sẵn Maven để build file .jar
#FROM maven:3.8.5-openjdk-17 AS build
## Đặt thư mục làm việc bên trong container
#WORKDIR /app
## Copy file pom.xml trước để tận dụng cache của Docker
#COPY pom.xml .
## Tải các dependency về
#RUN mvn dependency:go-offline
## Copy toàn bộ mã nguồn còn lại
#COPY src ./src\
## Build ứng dụng, bỏ qua các bài test để build nhanh hơn
#RUN mvn clean package -DskipTests
#
## Giai đoạn 2: Chạy ứng dụng
## Sử dụng một image Java nhỏ gọn hơn để chạy, giúp image cuối cùng nhẹ hơn
#FROM openjdk:17-jdk-slim
## Đặt thư mục làm việc
#WORKDIR /app
## Copy file .jar đã được build từ giai đoạn 1 sang
#COPY --from=build /app/target/cinemabook-0.0.1-SNAPSHOT.jar app.jar
## Mở cổng 8080 để bên ngoài có thể truy cập vào ứng dụng
#EXPOSE 8080
## Lệnh để khởi chạy ứng dụng khi container bắt đầu
#ENTRYPOINT ["java", "-jar", "app.jar"]