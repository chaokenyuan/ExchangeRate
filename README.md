# 匯率服務系統 (Exchange Rate Service)

## 專案簡介

這是一個基於 Spring Boot 的匯率查詢與轉換服務系統，提供了完整的 RESTful API 來管理和查詢各種貨幣之間的匯率資料。系統使用 H2 記憶體資料庫作為資料儲存，適合用於開發、測試環境或小型應用。

## 技術棧

- **Java 17** - 程式語言
- **Spring Boot 3.2.0** - 應用程式框架
- **Spring Data JPA** - 資料持久層
- **H2 Database** - 記憶體資料庫
- **Lombok** - 減少樣板程式碼
- **Maven** - 專案管理工具

## 系統架構

### 專案結構
```
ExchangeRate/
├── pom.xml                              # Maven 設定檔
└── src/
    └── main/
        ├── java/com/exchangerate/
        │   ├── ExchangeRateApplication.java     # 主程式入口
        │   ├── config/
        │   │   └── DataInitializer.java        # 資料初始化設定
        │   ├── controller/
        │   │   └── ExchangeRateController.java # REST API 控制器
        │   ├── model/
        │   │   └── ExchangeRate.java           # 匯率實體模型
        │   ├── repository/
        │   │   └── ExchangeRateRepository.java # 資料存取層
        │   └── service/
        │       └── ExchangeRateService.java    # 業務邏輯層
        └── resources/
            └── application.properties          # 應用程式設定
```

### 核心功能

1. **匯率管理**
   - 新增匯率資料
   - 更新現有匯率
   - 刪除匯率記錄
   - 查詢所有匯率

2. **匯率查詢**
   - 根據 ID 查詢特定匯率
   - 查詢兩種貨幣間的最新匯率
   - 根據來源貨幣查詢
   - 根據目標貨幣查詢

3. **貨幣轉換**
   - 即時計算貨幣轉換金額
   - 自動使用最新匯率
   - 精確到小數點後兩位

## API 端點說明

### 基礎路徑
`http://localhost:8080/api/exchange-rates`

### 端點列表

| 方法 | 路徑 | 說明 | 參數 |
|------|------|------|------|
| GET | `/` | 取得所有匯率資料 | - |
| GET | `/{id}` | 根據 ID 取得匯率 | id: 匯率記錄 ID |
| GET | `/rate` | 取得兩種貨幣間最新匯率 | from: 來源貨幣<br>to: 目標貨幣 |
| GET | `/convert` | 轉換貨幣金額 | from: 來源貨幣<br>to: 目標貨幣<br>amount: 金額 |
| POST | `/` | 新增匯率資料 | Request Body: ExchangeRate JSON |
| PUT | `/{id}` | 更新匯率資料 | id: 匯率記錄 ID<br>Request Body: ExchangeRate JSON |
| DELETE | `/{id}` | 刪除匯率資料 | id: 匯率記錄 ID |

### API 使用範例

#### 1. 查詢最新匯率
```bash
GET /api/exchange-rates/rate?from=USD&to=EUR
```

#### 2. 貨幣轉換
```bash
GET /api/exchange-rates/convert?from=USD&to=EUR&amount=100
```

#### 3. 新增匯率資料
```bash
POST /api/exchange-rates
Content-Type: application/json

{
  "fromCurrency": "USD",
  "toCurrency": "TWD",
  "rate": 31.25,
  "source": "Central Bank"
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

## 開發與測試

### 日誌設定
- 應用程式日誌級別：DEBUG
- Spring Web 日誌級別：INFO
- SQL 語句顯示：啟用
- SQL 格式化：啟用

### 熱部署
專案已配置 Spring DevTools，支援開發時的熱部署功能。

## 後續擴充建議

1. **安全性增強**
   - 加入 Spring Security 進行 API 認證授權
   - 實作 JWT Token 機制
   - 加入 API 限流功能

2. **功能擴充**
   - 整合第三方匯率 API（如 OpenExchangeRates）
   - 加入匯率歷史記錄查詢
   - 實作匯率趨勢分析
   - 加入多語言支援

3. **效能優化**
   - 實作快取機制（Redis）
   - 改用持久化資料庫（MySQL/PostgreSQL）
   - 加入資料庫連線池配置

4. **監控與維運**
   - 整合 Spring Actuator 進行健康檢查
   - 加入 API 文件（Swagger/OpenAPI）
   - 實作日誌集中管理
   - 加入效能監控指標

## 授權資訊

[請根據實際情況填寫授權資訊]

## 聯絡資訊

[請填寫維護者聯絡資訊]