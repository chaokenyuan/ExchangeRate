# 匯率服務系統 (Exchange Rate Service)

## 專案簡介

這是一個基於 Spring Boot 的匯率查詢與轉換服務系統，採用BDD (行為驅動開發) 方法論開發。系統提供完整的 RESTful API 來管理和查詢各種貨幣之間的匯率資料，並支援即時貨幣換算功能。

## 🛠️ 技術棧

**核心框架**
- **Java 17** - 程式語言
- **Spring Boot 3.2.0** - 主要應用程式框架
- **Spring Data JPA** - 資料持久層
- **Spring Boot Actuator** - 應用程式監控
- **Maven 3.9+** - 專案構建工具

**資料庫**
- **H2 Database** - 開發/測試環境記憶體資料庫
- **MySQL/PostgreSQL** - 生產環境資料庫選項

**測試框架**
- **JUnit 5** - 單元測試框架
- **Mockito** - 模擬測試框架
- **Spring Boot Test** - 整合測試
- **Cucumber-JVM 7.15.0** - BDD測試框架
- **REST Assured 5.3.2** - REST API測試
- **TestContainers** - 容器化測試

**開發工具**
- **Lombok** - 減少樣板程式碼
- **Jackson** - JSON處理
- **Hibernate Validator** - 資料驗證
- **SpringDoc OpenAPI 3** - API文檔生成 (Swagger)

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
    │   ├── ExchangeRateApplication.java      # Spring Boot主程式
    │   ├── config/DataInitializer.java       # 資料初始化
    │   ├── controller/                       # REST API控制器層
    │   │   ├── ExchangeRateController.java   # 匯率CRUD API
    │   │   └── ConversionController.java     # 貨幣轉換API
    │   ├── dto/                              # 數據傳輸對象
    │   │   ├── ConversionRequest.java        # 轉換請求模型
    │   │   └── ConversionResponse.java       # 轉換回應模型
    │   ├── model/ExchangeRate.java           # JPA實體模型
    │   ├── repository/ExchangeRateRepository.java # Spring Data JPA存取層
    │   └── service/ExchangeRateService.java  # 業務邏輯層
    ├── main/resources/
    │   └── application.properties            # Spring Boot配置
    └── test/                                 # 測試程式碼
        ├── java/com/exchangerate/
        │   ├── CucumberTestRunner.java       # Cucumber測試執行器
        │   ├── ExchangeRateApplicationTests.java # Spring Boot測試
        │   ├── config/CucumberSpringConfiguration.java # Cucumber Spring配置
        │   ├── hooks/                        # Cucumber測試掛鉤
        │   │   ├── ApiHooks.java            # API測試掛鉤
        │   │   └── DatabaseHooks.java       # 資料庫測試掛鉤
        │   └── stepdefinitions/              # Cucumber步驟定義
        │       └── ExchangeRateStepDefinitions.java # BDD測試步驟實作
        └── resources/
            ├── application-test.properties   # 測試環境配置
            └── features/                     # Gherkin測試規格
                └── exchange-rate-api.feature # 匯率API功能測試規格
```

### 核心功能

基於TDD開發的完整匯率API系統，具備以下功能：

**1. CRUD匯率管理**
- ✅ 新增匯率資料 (POST /api/exchange-rates)
- ✅ 查詢所有匯率 (GET /api/exchange-rates)
- ✅ 根據ID查詢 (GET /api/exchange-rates/{id})
- ✅ 特定匯率對查詢 (GET /api/exchange-rates/{from}/{to})
- ✅ 更新匯率資料 (PUT /api/exchange-rates/{from}/{to})
- ✅ 刪除匯率資料 (DELETE /api/exchange-rates/{from}/{to})

**2. 智慧貨幣換算**
- ✅ 詳細轉換API (POST /api/convert)
- ✅ 直接匯率轉換 (USD→TWD)
- ✅ 反向匯率計算 (TWD→USD = 1/rate)
- ✅ 鏈式中介轉換 (EUR→USD→TWD)
- ✅ 精確度控制 (BigDecimal 6位小數)

**3. 高級查詢功能**
- ✅ 過濾條件查詢 (?from=USD&to=TWD)
- ✅ 分頁查詢支援 (?page=1&limit=50)
- ✅ 分頁元數據 (total_pages, total_records, has_next)
- ✅ 靈活回應格式 (陣列或分頁物件)



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
| POST | `/api/exchange-rates` | 新增匯率資料 | Request Body: ExchangeRate JSON |
| PUT | `/api/exchange-rates/{from}/{to}` | 更新特定匯率對 | from, to: 貨幣對<br>Request Body: 更新資料 |
| DELETE | `/api/exchange-rates/{from}/{to}` | 刪除特定匯率對 | from, to: 貨幣對 |
| POST | `/api/convert` | 智慧貨幣轉換 | Request Body: ConversionRequest JSON |

### API 使用範例

#### 1. 查詢特定匯率對
```bash
GET /api/exchange-rates/USD/TWD
```

#### 2. 智慧貨幣轉換
```bash
POST /api/convert
Content-Type: application/json

{
  "from_currency": "USD",
  "to_currency": "TWD", 
  "amount": 100
}
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
PUT /api/exchange-rates/USD/TWD
Content-Type: application/json

{
  "rate": 33.0
}
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
- 使用 H2 記憶體資料庫
- 資料庫名稱：exchangeratedb
- 使用者名稱：sa
- 密碼：（空）

### H2 Console
- 啟用狀態：是
- 存取路徑：`http://localhost:8080/h2-console`
- JDBC URL：`jdbc:h2:mem:exchangeratedb`

## 快速開始

### 系統需求
- JDK 17 或以上版本
- Maven 3.6 或以上版本

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
- API 服務：`http://localhost:8080/api/exchange-rates`
- Swagger UI：`http://localhost:8080/swagger-ui/index.html`
- OpenAPI 文檔：`http://localhost:8080/v3/api-docs`
- H2 Console：`http://localhost:8080/h2-console`

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

## 特色功能

1. **自動大寫轉換**：貨幣代碼會自動轉換為大寫，確保資料一致性
2. **時間戳記管理**：自動記錄資料建立和更新時間
3. **精確計算**：使用 BigDecimal 確保匯率計算精確度
4. **RESTful 設計**：符合 REST 架構風格的 API 設計
5. **交易管理**：使用 Spring 的 @Transactional 確保資料一致性


## 授權資訊

[請根據實際情況填寫授權資訊]

## 聯絡資訊

[請填寫維護者聯絡資訊]