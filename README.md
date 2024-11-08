# 打卡系統

## 簡介
這是一個基於 Spring Boot 和 Thymeleaf 的打卡系統，提供用戶打卡、顯示地圖和打卡記錄的功能。

## 技術棧
- Java
- Spring Boot
- Thymeleaf
- Maven
- JavaScript
- Leaflet.js

## 功能
- 用戶打卡
- 顯示用戶與公司位置的距離
- 顯示打卡記錄

## 項目結構
```plaintext
├─java
│  └─com
│      └─example
│          └─clockin
│              ├─config
│              ├─controller
│              ├─dto
│              ├─model
│              ├─repo
│              ├─service
│              └─util
```
## 安裝與運行
1. 克隆項目到本地：
    ```sh
    git clone https://github.com/your-repo/attendance-system.git
    cd attendance-system
    ```

2. 使用 Maven 構建項目：
    ```sh
    mvn clean install
    ```

3. 運行 Spring Boot 應用：
    ```sh
    mvn spring-boot:run
    ```

4. 在瀏覽器中打開 `http://localhost:8080` 查看應用。

## 配置
在 `src/main/resources/application.properties` 文件中配置數據庫連接和其他設置。

## 依賴
- Spring Boot Starter Web
- Spring Boot Starter Thymeleaf
- Spring Boot Starter Data JPA
- H2 Database
- Maven

## 使用
### 打卡
1. 打開應用首頁。
2. 點擊 "打卡" 按鈕。
3. 系統將顯示用戶與公司位置的距離並記錄打卡時間。

### 查看打卡記錄
1. 打開應用首頁。
2. 在 "過去的打卡記錄" 部分查看打卡記錄。
