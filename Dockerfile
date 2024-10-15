# 第一階段：建構階段
FROM maven:3.8.5-openjdk-17 AS build

# 設定工作目錄
WORKDIR /app

# 複製 Maven 的 POM 文件和專案代碼到容器中
COPY pom.xml .
COPY src ./src

# 使用 Maven 打包應用程式
RUN mvn clean package -DskipTests

# 第二階段：運行階段
FROM openjdk:17-jdk-slim

# 設定工作目錄
WORKDIR /app

# 從第一階段複製已構建的 JAR 文件到運行階段
COPY --from=build /app/target/clockin-0.0.1-SNAPSHOT.jar app.jar

# 複製 application.properties
COPY src/main/resources/application.properties /app/config/application.properties

# 設定容器啟動時執行的命令，並指定 application.properties 路徑
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/config/application.properties"]

# 指定容器開放的端口
EXPOSE 8080
