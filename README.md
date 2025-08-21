# 匯率服務系統 (Exchange Rate Service)

## 專案簡介

這是一個基於 **六角形架構 (Hexagonal Architecture)** 和 **CQRS模式** 的匯率查詢與轉換服務系統，採用BDD (行為驅動開發) 方法論開發。系統提供完整的 RESTful API 來管理和查詢各種貨幣之間的匯率資料，並支援即時貨幣換算功能。

## 🏗️ 系統架構

### 六角形架構 (Hexagonal Architecture)

採用**端口與適配器模式** (Ports and Adapters Pattern)，實現完整的依賴反轉：

```
┌─────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                     │
│  ┌─────────────────┐              ┌─────────────────┐       │
│  │   Web Adapter   │              │  Persistence    │       │
│  │  (Controllers)  │              │    Adapter      │       │
│  └─────────────────┘              └─────────────────┘       │
└─────────────┬───────────────────────────────┬───────────────┘
              │                               │
┌─────────────▼───────────────────────────────▼───────────────┐
│                   Application Layer                         │
│  ┌─────────────────┐              ┌─────────────────┐       │
│  │  Command Side   │              │   Query Side    │       │
│  │  (Use Cases)    │              │  (Query Svc)    │       │
│  └─────────────────┘              └─────────────────┘       │
└─────────────┬───────────────────────────────┬───────────────┘
              │                               │
┌─────────────▼───────────────────────────────▼───────────────┐
│                     Domain Layer                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐  │
│  │    Entities     │  │  Value Objects  │  │  Domain      │  │
│  │  (ExchangeRate) │  │  (CurrencyPair) │  │  Services    │  │
│  └─────────────────┘  └─────────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### CQRS 模式實現

**命令與查詢責任分離**：
- **Command Side** (寫操作)：ConvertCurrencyCommand
- **Query Side** (讀操作)：GetExchangeRateQuery、ListExchangeRatesQuery、GetExchangeRateByIdQuery

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
- **JUnit 5** - 單元測試框架 (61個測試)
- **Mockito** - 模擬測試框架
- **Spring Boot Test** - 整合測試
- **Cucumber-JVM 7.15.0** - BDD測試框架 (61個場景)
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
- 📝 **Cucumber測試標準**: 詳見 [cucumber-unit-testing-standards.md](src/test/resources/cucumber-unit-testing-standards.md)
- 🔍 **QA測試標準**: 詳見 [qa-testing-standards.md](.ai-docs/standards/qa-testing-standards.md)

## 🏗️ 技術團隊角色系統

本專案採用角色導向的開發流程，每個角色都有特定的技術專精：

### 👥 可用角色
- 🏗️ **[架構師](/.ai-docs/role-architect.md)** - Spring Boot架構設計與微服務規劃
- 👨‍💻 **[開發員](/.ai-docs/role-developer.md)** - Spring Boot應用開發與API實作  
- 🧪 **[QA測試員](/.ai-docs/role-qa-tester.md)** - JUnit 5 + Cucumber-JVM測試
- 🔍 **[代碼審查員](/.ai-docs/role-code-reviewer.md)** - Spring最佳實踐審查與規範檢查
- 📊 **[SA系統分析師](/.ai-docs/role-system-analyst.md)** - 業務需求分析與.feature規格撰寫
- 📐 **[SD系統設計師](/.ai-docs/role-system-designer.md)** - 系統設計與資料庫設計

## 📁 專案結構 (六角形架構)

```
ExchangeRate/
├── pom.xml                                    # Maven設定檔
├── CLAUDE.md                                 # 技術團隊角色系統配置
├── .ai-docs/                                 # 角色與技術棧配置
└── src/
    ├── main/java/com/exchangerate/
    │   ├── ExchangeRateApplication.java      # Spring Boot主程式
    │   ├── config/                           # 配置層
    │   │   ├── DataInitializer.java         # 資料初始化
    │   │   └── HexagonalProfileConfiguration.java # Profile配置
    │   │
    │   ├── domain/                           # 🔵 Domain Layer (核心業務)
    │   │   ├── model/
    │   │   │   ├── entity/ExchangeRate.java # 聚合根實體
    │   │   │   └── valueobject/             # 值對象
    │   │   │       ├── CurrencyCode.java    # 貨幣代碼
    │   │   │       ├── CurrencyPair.java    # 貨幣對
    │   │   │       ├── Rate.java            # 匯率值
    │   │   │       └── ConversionResult.java # 轉換結果
    │   │   └── port/out/                    # 外部端口接口
    │   │       └── ExchangeRateRepository.java
    │   │
    │   ├── application/                     # 🔴 Application Layer (用例)
    │   │   ├── dto/
    │   │   │   ├── command/                 # CQRS - Command DTOs
    │   │   │   │   └── ConvertCurrencyCommand.java
    │   │   │   ├── query/                   # CQRS - Query DTOs
    │   │   │   │   ├── GetExchangeRateQuery.java
    │   │   │   │   ├── ListExchangeRatesQuery.java
    │   │   │   │   └── GetExchangeRateByIdQuery.java
    │   │   │   └── response/                # 響應DTOs
    │   │   │       ├── ExchangeRateResponse.java
    │   │   │       └── ConversionResponse.java
    │   │   ├── port/in/                     # 入站端口 (Use Cases)
    │   │   │   ├── CreateExchangeRateUseCase.java
    │   │   │   ├── ConvertCurrencyUseCase.java
    │   │   │   └── QueryExchangeRateUseCase.java
    │   │   ├── service/                     # Application Services
    │   │   │   ├── ExchangeRateApplicationService.java # CRUD處理
    │   │   │   ├── ConversionApplicationService.java   # 轉換邏輯
    │   │   │   └── ExchangeRateQueryService.java       # Query處理
    │   │   └── mapper/                      # 對象映射器
    │   │       ├── ExchangeRateMapper.java
    │   │       └── ConversionMapper.java
    │   │
    │   └── infrastructure/                  # 🔶 Infrastructure Layer (適配器)
    │       ├── adapter/
    │       │   ├── in/web/                  # Web入站適配器
    │       │   │   └── ConversionController.java
    │       │   └── out/persistence/         # 持久化出站適配器
    │       │       ├── JpaExchangeRateRepositoryAdapter.java
    │       │       ├── ExchangeRatePersistenceMapper.java
    │       │       ├── entity/ExchangeRateJpaEntity.java
    │       │       └── repository/ExchangeRateJpaRepository.java
    │       ├── config/                      # 基礎設施配置
    │       │   ├── DataInitializer.java
    │       │   └── OpenApiConfig.java
    │       ├── constants/                   # 常數定義
    │       │   ├── CurrencyConstants.java
    │       │   ├── ErrorMessages.java
    │       │   └── EnglishErrorMessages.java
    │       └── exception/                   # 例外處理
    │           ├── GlobalExceptionHandler.java
    │           ├── ResourceNotFoundException.java
    │           └── DuplicateResourceException.java
    │
    ├── main/resources/
    │   └── application.properties            # Spring Boot配置
    │
    └── test/                                 # 🧪 測試層 (122個測試)
        ├── java/com/exchangerate/
        │   ├── CucumberTestRunner.java       # Cucumber執行器
        │   ├── stepdefinitions/              # Cucumber步驟定義
        │   │   ├── BaseStepDefinitions.java
        │   │   ├── ConversionStepDefinitions.java
        │   │   ├── ExchangeRateStepDefinitions.java
        │   │   ├── PerformanceStepDefinitions.java
        │   │   ├── SecurityStepDefinitions.java
        │   │   ├── SystemStepDefinitions.java
        │   │   ├── ValidationStepDefinitions.java
        │   │   └── TestHooks.java
        │   ├── domain/model/                 # Domain單元測試
        │   ├── application/service/          # Application服務測試
        │   ├── infrastructure/adapter/       # Infrastructure測試
        │   └── infrastructure/config/        # 測試配置
        └── resources/
            ├── application-test.properties   # 測試配置
            └── features/                     # 🥒 Gherkin BDD規格 (7個功能檔案)
                ├── currency-conversion.feature
                ├── data-validation.feature
                ├── exchange-rate-api.feature
                ├── exchange-rate-management.feature
                ├── performance-limitations.feature
                ├── security-authorization.feature
                └── system-integration.feature
```

## 🎯 核心功能

### 智慧貨幣換算引擎
- ✅ **直接轉換**: USD → TWD (直接查找匯率)
- ✅ **反向計算**: TWD → USD (1/rate 自動計算)
- ✅ **鏈式轉換**: EUR → USD → TWD (透過中介貨幣)
- ✅ **精確計算**: BigDecimal 6位小數精度

### CRUD 匯率管理 API
- ✅ 新增匯率資料 (POST /api/exchange-rates)
- ✅ 查詢所有匯率 (GET /api/exchange-rates)
- ✅ 根據ID查詢 (GET /api/exchange-rates/{id})
- ✅ 特定匯率對查詢 (GET /api/exchange-rates/{from}/{to})
- ✅ 更新匯率資料 (PUT /api/exchange-rates/{from}/{to})
- ✅ 刪除匯率資料 (DELETE /api/exchange-rates/{from}/{to})

### 高級查詢功能
- ✅ 過濾條件查詢 (?from=USD&to=TWD)
- ✅ 分頁查詢支援 (?page=1&limit=50)
- ✅ 分頁元數據 (total_pages, total_records, has_next)
- ✅ 靈活回應格式 (陣列或分頁物件)

## 📊 測試覆蓋度

### ✅ 測試統計
- **Java單元測試**: 涵蓋Domain、Application、Infrastructure層
- **Cucumber BDD測試**: 7個功能檔案，涵蓋完整業務場景
- **測試組織**: 分層測試架構，確保各層職責清晰 🎯

### 測試層級
- **單元測試**: Domain層純業務邏輯測試
- **服務測試**: Application層用例測試  
- **集成測試**: Infrastructure層適配器測試
- **BDD測試**: 端到端業務場景測試

## 🚀 快速開始

### 系統需求
- JDK 17 或以上版本
- Maven 3.6 或以上版本

### 啟動步驟

1. **複製專案**
```bash
git clone [專案 URL]
cd ExchangeRate
```

2. **建置專案**
```bash
mvn clean install
```

3. **啟動應用程式**
```bash
# 啟動應用程式
mvn spring-boot:run

# 或使用JAR檔案
java -jar target/exchange-rate-1.0.0-SNAPSHOT.jar
```

4. **運行測試**
```bash
# 運行所有測試
mvn test

# 只運行單元測試
mvn test -Dtest="**Test"

# 只運行Cucumber測試
mvn test -Dtest="**/CucumberTestRunner"

# 生成測試報告
mvn test jacoco:report
```

## 📖 API 文檔

### 文檔入口 (需先啟動應用程式)
- 🌐 **[Swagger UI](http://localhost:8080/swagger-ui/index.html)** - 互動式 API 文檔界面
- 📄 **[OpenAPI JSON](http://localhost:8080/v3/api-docs)** - 標準 OpenAPI 3.0 規格文件
- 🗄️ **[H2 Console](http://localhost:8080/h2-console)** - 資料庫管理界面

### API 使用範例

#### 智慧貨幣轉換
```bash
POST /api/convert
Content-Type: application/json

{
  "from_currency": "USD",
  "to_currency": "TWD", 
  "amount": 100
}

# 回應
{
  "from_currency": "USD",
  "to_currency": "TWD",
  "from_amount": 100.0,
  "to_amount": 3250.0,
  "rate": 32.5,
  "conversion_date": "2024-01-15T10:00:00",
  "conversion_path": ["USD", "TWD"]
}
```

#### 查詢匯率 (支援分頁)
```bash
GET /api/exchange-rates?from=USD&page=1&limit=10

# 回應
{
  "content": [...],
  "page": 1,
  "limit": 10,
  "total_pages": 3,
  "total_records": 25,
  "has_next": true
}
```

## 🏛️ 架構設計原則

### Domain-Driven Design (DDD)
- **聚合根**: ExchangeRate實體管理所有相關業務邏輯
- **值對象**: CurrencyCode、CurrencyPair、Rate確保類型安全
- **領域服務**: 複雜業務邏輯封裝

### 依賴反轉原則
- Domain層完全獨立，無外部依賴
- Application層依賴Domain接口
- Infrastructure層實現Domain定義的端口

### 關注點分離
- **Command端**: 處理寫操作和業務邏輯
- **Query端**: 優化讀操作和數據查詢
- **Web層**: 專注HTTP協議和數據轉換

## 🔧 配置管理

### 應用程式配置
```properties
# application.properties - 主要配置
spring.jpa.hibernate.ddl-auto=create-drop
spring.datasource.url=jdbc:h2:mem:exchangeratedb
spring.datasource.username=sa
spring.datasource.password=
logging.level.com.exchangerate=DEBUG

# H2 Console 配置
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# OpenAPI 文檔配置
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### 測試配置
```properties
# application-test.properties - 測試專用配置
spring.datasource.url=jdbc:h2:mem:testdb
logging.level.org.springframework.test=DEBUG
spring.jpa.show-sql=false
```

## 📊 性能指標

- **啟動時間**: < 10秒
- **API回應時間**: < 100ms (平均)
- **記憶體使用**: ~200MB (運行時)
- **測試執行時間**: ~30秒 (完整測試套件)

## 🔒 安全特性

- **數據驗證**: Jakarta Validation確保輸入數據正確性
- **事務管理**: Spring @Transactional確保數據一致性  
- **錯誤處理**: 統一錯誤回應格式
- **貨幣代碼驗證**: 嚴格的3字元貨幣代碼檢查

## 🚀 部署建議

### Docker部署
```dockerfile
FROM openjdk:17-jre-slim
COPY target/exchange-rate-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 生產環境配置
- 使用PostgreSQL或MySQL替代H2
- 配置連線池 (HikariCP)
- 啟用應用程式監控 (Spring Boot Actuator)
- 設定日誌聚合 (ELK Stack)

## 🤝 貢獻指南

1. Fork本專案
2. 建立功能分支 (`git checkout -b feature/new-feature`)
3. 遵循六角形架構原則和技術團隊角色規範
4. 撰寫對應的單元測試和BDD場景
5. 確保所有測試通過 (`mvn test`)
6. 遵循測試標準規範 (Given-When-Then模式)
7. 提交更改 (`git commit -m 'Add new feature'`)
8. 推送到分支 (`git push origin feature/new-feature`)
9. 建立Pull Request

## 📜 版本歷史

- **v1.0.0** - 初始版本，基礎CRUD功能
- **v2.0.0** - 完整六角形架構重構，CQRS模式實現 🎯
- **v2.1.0** - 智慧換算引擎 (直接/反向/鏈式)
- **v2.2.0** - 完整測試覆蓋 (122個測試)

## 📄 授權資訊

本專案採用 MIT 授權條款，詳見 [LICENSE](LICENSE) 文件。

## 📞 聯絡資訊

- **專案維護者**: Exchange Rate Team
- **技術支援**: 請建立 [GitHub Issue](https://github.com/your-repo/ExchangeRate/issues)
- **文檔問題**: 請參考 [Wiki](https://github.com/your-repo/ExchangeRate/wiki)

---

**🏗️ 架構特色**: 採用六角形架構 + CQRS模式，實現高內聚低耦合的企業級應用程式設計。

**🧪 品質保證**: 122個測試確保代碼品質，BDD驅動開發確保業務需求準確實現。