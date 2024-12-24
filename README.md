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
com.example.clockin
 ├─ config
 ├─ controller
 ├─ dto
 ├─ model
 ├─ repo
 ├─ service
 │   ├─ facade
 │   │   └─ AttendanceFacade.java
 │   ├─ factory
 │   │   └─ ClockInFactory.java
 │   ├─ AttendanceService.java
 │   ├─ RecordQueryService.java
 │   └─ ...
 └─ util
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


### security 配置

以下概念性說明示範如何在同一個 Spring Boot 專案中，使用多條 `SecurityFilterChain` 同時支援：
1. **RESTful API** (以 JWT Token 進行驗證)
2. **Web 前端** (以傳統表單登入 / Session 驗證)

並且透過適度拆分與共用，避免循環依賴 (Circular Dependency) 與重複定義 Bean。

---

## 核心觀念

1. **共用的核心 Bean**
   - 通常包含 `UserDetailsService`、`PasswordEncoder` (如 `BCryptPasswordEncoder`)、或 `JwtUtil` 等。
   - 這些 Bean 若散落在不同的 `@Configuration` 中，很可能造成「相互注入」而形成循環依賴；因此可放在「共用配置」裡集中管理，一次注入到各需要的地方。

2. **JWT 驗證的 `OncePerRequestFilter`**
   - 在每次 API 請求時檢查 `Authorization` 標頭，若有 Bearer Token，便解析並驗證。
   - 若 Token 有效，設定 `SecurityContextHolder` 以表明「當前已認證」；若無效或無 Token，則略過此步驟。

3. **ApiSecurityConfig (第一條 SecurityFilterChain)**
   - 一般只攔截 `/api/**` 路徑，並關閉 Session（`SessionCreationPolicy.STATELESS`）。
   - 允許像 `/api/login`、`/api/register` 這類不需 Token 的路徑；其它則必須帶 JWT Token。

4. **WebSecurityConfig (第二條 SecurityFilterChain)**
   - 負責「表單登入 / Web 頁面」的邏輯。例如 `.formLogin()`、`.logout()`、角色與權限控制 (`hasRole("ADMIN")` / `hasRole("USER")`) 等。
   - 可使用 `DaoAuthenticationProvider` 來搭配先前的 `UserDetailsService` 與 `PasswordEncoder`。

5. **指定順序**
   - 透過 `@Order` (或其他設定方式) 控制兩條 `SecurityFilterChain` 的優先級。
   - 例如 `ApiSecurityConfig` 設為 `@Order(1)`，讓 `/api/**` 請求先經過 JWT 驗證；
   - 其他路徑則走表單登入的 `WebSecurityConfig` (通常 `@Order(2)`)。

6. **避免 Bean 名稱與相依關係衝突**
   - 只需一個 `PasswordEncoder`、一個 `UserDetailsService`；不要在多個檔案中用同樣的 Bean 名稱重複定義。
   - Filter (如 `JwtRequestFilter`) 也改用「建構子注入」，避免隱形的循環依賴。

---

## 流程示意圖

以下以 Mermaid 流程圖簡單描述請求流程，並說明在多條 FilterChain 中怎麼決定去路：

```mermaid
flowchart TB
    A[接收到 HTTP Request] --> B{路徑是否以 /api/ 開頭?}
    B -- Yes --> C[進入 ApiSecurityConfig]
    C --> D[JwtRequestFilter 驗證 Token]
    D --> E{Token 有效?}
    E -- Yes --> F[SecurityContextHolder 設定認證]
    E -- No --> G[跳過認證或回應 401]
    F --> H[繼續後續處理 (Controller)]
    G --> H[繼續或被拒絕]

    B -- No --> X[進入 WebSecurityConfig]
    X --> Y[檢查 Session / Form Login]
    Y --> Z[依角色或權限判斷允許 / 拒絕]
    Z --> H2[進入對應的 Controller 或顯示 Error]
