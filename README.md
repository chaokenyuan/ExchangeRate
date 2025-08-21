# 匯率服務系統 (Exchange Rate Service)

## 專案簡介

這是一個基於 Spring Boot 3.2.0 的匯率查詢與轉換服務系統，採用 BDD (行為驅動開發) 方法論開發。系統提供完整的 RESTful API 來管理和查詢各種貨幣之間的匯率資料，並支援即時貨幣換算功能。

## 🛠️ 技術棧

**核心框架**
- **Java 17** - 程式語言
- **Spring Boot 3.2.0** - 主要應用程式框架
- **Spring Data JPA** - 資料持久層
- **Spring Boot Actuator** - 應用程式監控
- **Maven 3.9+** - 專案構建工具

**資料庫**
- **H2 Database** - 記憶體資料庫（內嵌模式）
- **JPA/Hibernate** - ORM 框架

**測試框架**
- **JUnit 5** - 單元測試框架
- **Mockito** - 模擬測試框架
- **Spring Boot Test** - 整合測試
- **Cucumber-JVM 7.15.0** - BDD測試框架
- **REST Assured 5.3.2** - REST API測試
- **TestContainers** - 容器化測試

**開發工具**
- **Lombok** - 減少樣板程式碼
- **Jackson** - JSON 處理
- **Hibernate Validator** - 資料驗證
- **SpringDoc OpenAPI 2.2.0** - API 文檔生成 (Swagger UI)
- **Spring Boot DevTools** - 開發時熱部署

## 📋 技術規範與約束

本專案採用嚴格的技術棧約束，確保開發一致性：

- 📋 **技術棧配置**: 詳見 [tech-stacks.md](.ai-docs/tech-stacks.md)
- 👨‍💻 **開發規範**: 詳見 [role-developer.md](.ai-docs/role-developer.md)  
- 🧪 **測試規範**: 詳見 [role-qa-tester.md](.ai-docs/role-qa-tester.md)


## 🏗️ 技術團隊角色系統

本專案採用角色導向的開發流程，每個角色都有特定的技術專精：

### 👥 可用角色
- 🏗️ **[架構師](/.ai-docs/role-architect.md)** - Spring Boot架構設計與微服務規劃
- 👨‍💻 **[開發員](/.ai-docs/role-developer.md)** - Spring Boot應用開發與API實作  
- 🧪 **[QA測試員](/.ai-docs/role-qa-tester.md)** - JUnit 5 + Cucumber-JVM測試
- 🔍 **[代碼審查員](/.ai-docs/role-code-reviewer.md)** - Spring最佳實踐審查與規範檢查
- 📊 **[SA系統分析師](/.ai-docs/role-system-analyst.md)** - 業務需求分析與.feature規格撰寫
- 📐 **[SD系統設計師](/.ai-docs/role-system-designer.md)** - 系統設計與資料庫設計

### 🔒 技術約束
詳細的技術約束和工作流程請參考：
- 📋 **[技術棧配置](/.ai-docs/tech-stacks.md)** - 鎖定Spring Boot生態系統
- 🔄 **[公共行動模式](/.ai-docs/common-action-patterns.md)** - 三階段工作流程
- 🎭 **[角色行動模式](/.ai-docs/role-action-patterns.md)** - 6角色專屬工作模式

### 📊 .ai-docs 架構關係圖
技術團隊文檔結構的 PlantUML 視覺化圖表：
- 🏗️ **[架構層次圖](/.ai-docs/ai-docs-architecture.puml)** - 4層架構的層次依賴關係圖

## 系統架構

### 專案結構
```
ExchangeRate/
├── pom.xml                                    # Maven設定檔
├── CLAUDE.md                                 # 技術團隊角色系統配置
├── .ai-docs/                                 # 角色與技術棧配置
│   ├── role-architect.md                     # 架構師角色定義
│   ├── role-developer.md                     # 開發員角色定義
│   ├── role-qa-tester.md                     # QA測試員角色定義
│   ├── role-code-reviewer.md                 # 代碼審查員角色定義
│   ├── role-system-analyst.md                # SA系統分析師角色定義
│   ├── role-system-designer.md               # SD系統設計師角色定義
│   ├── tech-stacks.md                        # 技術棧配置
│   ├── common-action-patterns.md             # 公共三階段工作流程
│   ├── role-action-patterns.md               # 6角色專屬行動模式
│   ├── ai-docs-relationships.puml            # .ai-docs 關係圖 (基本版)
│   ├── ai-docs-detailed-relationships.puml   # .ai-docs 詳細關係圖
│   └── ai-docs-architecture.puml             # .ai-docs 架構層次圖
└── src/
    ├── main/java/com/exchangerate/           # 主要應用程式碼
    │   ├── ExchangeRateApplication.java      # Spring Boot 主程式
    │   ├── config/                           # 設定檔
    │   │   ├── DataInitializer.java          # 資料初始化
    │   │   └── OpenApiConfig.java            # OpenAPI 設定
    │   ├── constants/                        # 常數定義
    │   │   ├── CurrencyConstants.java        # 貨幣常數
    │   │   ├── ErrorMessages.java            # 錯誤訊息（中文）
    │   │   └── EnglishErrorMessages.java     # 錯誤訊息（英文）
    │   ├── controller/                       # REST API 控制器層
    │   │   ├── ExchangeRateController.java   # 匯率 CRUD API
    │   │   └── ConversionController.java     # 貨幣轉換 API
    │   ├── dto/                              # 數據傳輸對象
    │   │   ├── ConversionRequest.java        # 轉換請求模型
    │   │   └── ConversionResponse.java       # 轉換回應模型
    │   ├── exception/                        # 例外處理
    │   │   ├── GlobalExceptionHandler.java   # 全域例外處理器
    │   │   ├── ResourceNotFoundException.java # 資源未找到例外
    │   │   └── DuplicateResourceException.java # 重複資源例外
    │   ├── model/                            # 實體模型
    │   │   └── ExchangeRate.java             # JPA 實體模型
    │   ├── repository/                       # 資料存取層
    │   │   └── ExchangeRateRepository.java   # Spring Data JPA 存取層
    │   └── service/                          # 業務邏輯層
    │       └── ExchangeRateService.java      # 匯率服務
    ├── main/resources/
    │   └── application.properties            # Spring Boot配置
    └── test/                                 # 測試程式碼
        ├── java/com/exchangerate/
        │   ├── CucumberTestRunner.java       # Cucumber 測試執行器
        │   ├── config/                       # 測試設定
        │   │   ├── TestSecurityInterceptor.java # 測試安全攔截器
        │   │   └── TestWebConfig.java        # 測試 Web 設定
        │   ├── controller/                   # 控制器測試
        │   │   └── ConversionControllerTest.java # 轉換控制器測試
        │   ├── dto/                          # DTO 測試
        │   │   └── ConversionRequestTest.java # 轉換請求測試
        │   ├── mock/                         # Mock 服務
        │   │   ├── MockExchangeRateService.java # Mock 匯率服務
        │   │   └── MockServiceFactory.java   # Mock 服務工廠
        │   ├── model/                        # 模型測試
        │   │   └── ExchangeRateTest.java     # 匯率實體測試
        │   ├── service/                      # 服務測試
        │   │   ├── ExchangeRateServiceTest.java # 匯率服務測試
        │   │   └── TestExchangeRateService.java # 測試用匯率服務
        │   └── stepdefinitions/              # Cucumber 步驟定義
        │       ├── MockBasedStepDefinitions.java # Mock 基礎步驟定義
        │       └── SessionContext.java       # 會話上下文
        └── resources/
            ├── application-test.properties   # 測試環境配置
            ├── junit-platform.properties     # JUnit 平台設定
            └── features/                     # Gherkin 測試規格
                ├── currency-conversion.feature # 貨幣轉換功能測試
                ├── data-validation.feature   # 資料驗證測試
                ├── exchange-rate-api.feature # 匯率 API 功能測試
                ├── exchange-rate-management.feature # 匯率管理測試
                ├── performance-limitations.feature # 效能限制測試
                ├── security-authorization.feature # 安全授權測試
                └── system-integration.feature # 系統整合測試
```

### 核心功能

基於 BDD/TDD 開發的完整匯率 API 系統，具備以下功能：

**1. CRUD 匯率管理**
- ✅ 新增匯率資料 (POST /api/exchange-rates)
- ✅ 查詢所有匯率 (GET /api/exchange-rates)
- ✅ 根據 ID 查詢 (GET /api/exchange-rates/{id})
- ✅ 特定匯率對查詢 (GET /api/exchange-rates/{from}/{to})
- ✅ 更新匯率資料 (PUT /api/exchange-rates/{id} 或 PUT /api/exchange-rates/{from}/{to})
- ✅ 刪除匯率資料 (DELETE /api/exchange-rates/{id} 或 DELETE /api/exchange-rates/{from}/{to})

**2. 智慧貨幣換算**
- ✅ 詳細轉換 API (POST /api/convert)
- ✅ 簡易轉換 API (GET /api/exchange-rates/convert)
- ✅ 快速匯率查詢 (GET /api/exchange-rates/rate)
- ✅ 直接匯率轉換 (USD→TWD)
- ✅ 反向匯率計算 (TWD→USD = 1/rate)
- ✅ 鏈式中介轉換 (EUR→USD→TWD)
- ✅ 精確度控制 (BigDecimal 6 位小數)

**3. 高級查詢功能**
- ✅ 過濾條件查詢 (?from=USD&to=TWD)
- ✅ 分頁查詢支援 (?page=1&limit=50)
- ✅ 分頁元數據 (current_page, total_pages, total_records, has_next)
- ✅ 靈活回應格式 (陣列或分頁物件)

**4. 系統監控與管理**
- ✅ Spring Boot Actuator 健康檢查
- ✅ H2 Console 資料庫管理介面
- ✅ Swagger UI 互動式 API 文檔
- ✅ 自動資料初始化



## 📖 API 文檔

本專案提供完整的 Swagger/OpenAPI 3.0 文檔，支援線上測試：

### 文檔入口
- 🌐 **[Swagger UI](http://localhost:8080/swagger-ui/index.html)** - 互動式 API 文檔界面
- 📄 **[OpenAPI JSON](http://localhost:8080/v3/api-docs)** - 標準 OpenAPI 3.0 規格文件

### 文檔功能特色
- ✅ **中文界面** - 完整繁體中文說明
- ✅ **即時測試** - 直接在文檔中測試 API
- ✅ **範例資料** - 詳細的請求/回應範例
- ✅ **錯誤場景** - 完整的錯誤處理說明
- ✅ **資料模型** - 自動生成的 Schema 定義

## API 端點說明

### 基礎路徑
- **匯率管理**: `http://localhost:8080/api/exchange-rates`
- **貨幣轉換**: `http://localhost:8080/api/convert`

### 端點列表

| 方法 | 路徑 | 說明 | 參數 |
|------|------|------|------|
| GET | `/api/exchange-rates` | 取得所有匯率資料 | from, to, page, limit (可選) |
| GET | `/api/exchange-rates/{id}` | 根據 ID 取得匯率 | id: 匯率記錄 ID |
| GET | `/api/exchange-rates/{from}/{to}` | 取得特定匯率對 | from: 來源貨幣, to: 目標貨幣 |
| GET | `/api/exchange-rates/convert` | 簡易貨幣轉換 | from, to, amount (查詢參數) |
| GET | `/api/exchange-rates/rate` | 快速匯率查詢 | from, to (查詢參數) |
| POST | `/api/exchange-rates` | 新增匯率資料 | Request Body: ExchangeRate JSON |
| POST | `/api/exchange-rates/convert` | 詳細貨幣轉換（舊端點） | Request Body: ConversionRequest JSON |
| POST | `/api/convert` | 詳細貨幣轉換（主要端點） | Request Body: ConversionRequest JSON |
| PUT | `/api/exchange-rates/{id}` | 根據 ID 更新匯率 | id: 匯率記錄 ID<br>Request Body: 更新資料 |
| PUT | `/api/exchange-rates/{from}/{to}` | 更新特定匯率對 | from, to: 貨幣對<br>Request Body: 更新資料 |
| DELETE | `/api/exchange-rates/{id}` | 根據 ID 刪除匯率 | id: 匯率記錄 ID |
| DELETE | `/api/exchange-rates/{from}/{to}` | 刪除特定匯率對 | from, to: 貨幣對 |

### API 使用範例

#### 1. 查詢特定匯率對
```bash
GET /api/exchange-rates/USD/TWD
```

#### 2. 智慧貨幣轉換
```bash
# 詳細轉換（推薦）
POST /api/convert
Content-Type: application/json

{
  "from_currency": "USD",
  "to_currency": "TWD", 
  "amount": 100
}

# 簡易轉換
GET /api/exchange-rates/convert?from=USD&to=TWD&amount=100
```

#### 3. 新增匯率資料
```bash
POST /api/exchange-rates
Content-Type: application/json

{
  "from_currency": "USD",
  "to_currency": "TWD",
  "rate": 32.5,
  "source": "Central Bank"
}
```

#### 4. 分頁查詢匯率
```bash
GET /api/exchange-rates?page=1&limit=10&from=USD
```

#### 5. 更新匯率
```bash
# 根據貨幣對更新
PUT /api/exchange-rates/USD/TWD
Content-Type: application/json

{
  "rate": 33.0
}

# 根據 ID 更新
PUT /api/exchange-rates/1
Content-Type: application/json

{
  "rate": 33.0,
  "source": "Updated Bank"
}
```

#### 6. 刪除匯率
```bash
# 根據貨幣對刪除
DELETE /api/exchange-rates/USD/TWD

# 根據 ID 刪除
DELETE /api/exchange-rates/1
```

## 資料模型

### ExchangeRate 實體

| 欄位 | 類型 | 說明 | 限制 |
|------|------|------|------|
| id | Long | 主鍵 | 自動生成 |
| fromCurrency | String | 來源貨幣代碼 | 3 個字元，非空 |
| toCurrency | String | 目標貨幣代碼 | 3 個字元，非空 |
| rate | BigDecimal | 匯率 | 精度 19，小數 6 位 |
| timestamp | LocalDateTime | 時間戳記 | 自動生成 |
| source | String | 資料來源 | 最多 50 字元 |

## 環境設定

### 資料庫設定
- **資料庫類型**：H2 記憶體資料庫
- **資料庫名稱**：exchangeratedb
- **連接 URL**：jdbc:h2:mem:exchangeratedb
- **使用者名稱**：sa
- **密碼**：（空）
- **DDL 模式**：update（自動更新架構）

### H2 Console
- 啟用狀態：是
- 存取路徑：`http://localhost:8080/h2-console`
- JDBC URL：`jdbc:h2:mem:exchangeratedb`

## 快速開始

### 系統需求
- **JDK 17** 或以上版本（建議使用 JDK 17 或 21 LTS）
- **Maven 3.9** 或以上版本
- **記憶體**：至少 512MB
- **硬碟空間**：至少 100MB

### 安裝步驟

1. **複製專案**
```bash
git clone [專案 URL]
cd ExchangeRate
```

2. **建置專案**
```bash
mvn clean install
```

3. **執行應用程式**
```bash
mvn spring-boot:run
```

或者使用 JAR 檔案執行：
```bash
java -jar target/exchange-rate-1.0.0-SNAPSHOT.jar
```

4. **存取服務**
- **API 服務**：`http://localhost:8080/api/exchange-rates`
- **Swagger UI**：`http://localhost:8080/swagger-ui/index.html` 或 `http://localhost:8080/swagger-ui.html`
- **OpenAPI 文檔**：`http://localhost:8080/v3/api-docs`
- **H2 Console**：`http://localhost:8080/h2-console`
- **Actuator Health**：`http://localhost:8080/actuator/health`
- **Actuator Info**：`http://localhost:8080/actuator/info`
- **Actuator Metrics**：`http://localhost:8080/actuator/metrics`

## 初始資料

系統啟動時會自動載入以下預設匯率資料：

| 來源貨幣 | 目標貨幣 | 匯率 |
|----------|----------|------|
| USD | EUR | 0.92 |
| USD | GBP | 0.79 |
| USD | JPY | 149.50 |
| USD | CNY | 7.24 |
| USD | CHF | 0.88 |
| EUR | USD | 1.09 |
| EUR | GBP | 0.86 |
| GBP | USD | 1.27 |

這些資料由 `DataInitializer` 類在應用程式啟動時自動載入。

## 特色功能

1. **自動大寫轉換**：貨幣代碼會自動轉換為大寫，確保資料一致性
2. **時間戳記管理**：自動記錄資料建立和更新時間
3. **精確計算**：使用 BigDecimal 確保匯率計算精確度（6 位小數）
4. **RESTful 設計**：符合 REST 架構風格的 API 設計
5. **交易管理**：使用 Spring 的 @Transactional 確保資料一致性
6. **例外處理**：全域例外處理器提供友善的錯誤訊息
7. **資料驗證**：使用 Jakarta Validation 進行請求資料驗證
8. **熱部署支援**：開發環境支援 Spring Boot DevTools 熱部署
9. **BDD 測試**：完整的 Cucumber 測試覆蓋率
10. **API 文檔**：自動生成的 Swagger UI 互動式文檔


## 測試執行

### 執行所有測試
```bash
mvn test
```

### 執行 Cucumber BDD 測試
```bash
mvn test -Dtest=CucumberTestRunner
```

### 執行整合測試
```bash
mvn verify
```

## 故障排除

### 常見問題

1. **Bean 名稱衝突**
   - 執行 `mvn clean` 清理舊的編譯檔案
   - 重新編譯專案 `mvn compile`

2. **Port 8080 已被佔用**
   - 修改 `application.properties` 中的 `server.port` 設定
   - 或使用命令列參數：`java -jar target/exchange-rate-1.0.0-SNAPSHOT.jar --server.port=8081`

3. **H2 Console 無法存取**
   - 確認 `spring.h2.console.enabled=true`
   - 檢查防火牆設定

## 授權資訊

本專案採用 MIT 授權條款

## 聯絡資訊

專案維護者：[請填寫維護者資訊]
Email：[請填寫 Email]