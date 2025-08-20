# 六角形架構標準規範 v1.0

## 1. 總則

### 1.1 目的
本標準規範定義了在Spring Boot專案中實施六角形架構（Hexagonal Architecture）的標準做法，確保團隊開發的一致性和程式碼品質。

### 1.2 適用範圍
- 所有新建的Spring Boot微服務專案
- 重構現有單體應用為微服務架構
- 需要高度模組化和可測試性的企業應用

### 1.3 規範等級
- **必須（MUST）**：強制要求，違反將導致架構審查不通過
- **應該（SHOULD）**：強烈建議，需有充分理由才能違反
- **可以（MAY）**：選擇性建議，根據專案需求決定

## 2. 架構層次規範

### 2.1 層次劃分
專案**必須**劃分為以下三個主要層次：

```
┌─────────────────────────────────────────────────┐
│                Infrastructure Layer              │
│  ┌───────────────────────────────────────────┐  │
│  │          Application Layer                 │  │
│  │  ┌─────────────────────────────────────┐  │  │
│  │  │        Domain Layer                  │  │  │
│  │  │    (核心業務邏輯)                    │  │  │
│  │  └─────────────────────────────────────┘  │  │
│  │       (用例協調、事務管理)               │  │
│  └───────────────────────────────────────────┘  │
│         (技術實作、外部整合)                    │
└─────────────────────────────────────────────────┘
```

### 2.2 套件命名規範
根層套件**必須**遵循以下命名規則：

```
com.{company}.{project}/
├── domain/           # 領域層
├── application/      # 應用層
└── infrastructure/   # 基礎設施層
```

### 2.3 依賴規則
- Domain層**必須不**依賴Application層和Infrastructure層
- Application層**可以**依賴Domain層，**必須不**依賴Infrastructure層
- Infrastructure層**可以**依賴Domain層和Application層
- 跨層依賴**必須**透過介面（Port）進行

## 3. Domain Layer 規範

### 3.1 套件結構
```
domain/
├── model/              # 領域模型
│   ├── entity/        # 實體
│   ├── valueobject/   # 值物件
│   ├── aggregate/     # 聚合根
│   └── event/         # 領域事件
├── service/           # 領域服務
├── exception/         # 領域異常
└── port/              # 端口定義
    ├── in/            # 入站端口
    └── out/           # 出站端口
```

### 3.2 領域模型規範

#### 3.2.1 實體（Entity）
- **必須**有唯一標識符
- **應該**實作equals()和hashCode()基於ID
- **必須**包含業務行為方法，避免貧血模型

```java
public class Order {
    private OrderId id;           // 必須：唯一標識
    private OrderStatus status;    
    
    // 必須：業務行為方法
    public void confirm() {
        // 業務邏輯
    }
}
```

#### 3.2.2 值物件（Value Object）
- **必須**是不可變的（immutable）
- **必須**實作equals()和hashCode()基於所有屬性
- **應該**提供靜態工廠方法

```java
public final class Money {
    private final BigDecimal amount;
    private final Currency currency;
    
    private Money(BigDecimal amount, Currency currency) {
        // 驗證邏輯
        this.amount = amount;
        this.currency = currency;
    }
    
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }
}
```

#### 3.2.3 聚合根（Aggregate Root）
- **必須**是該聚合內唯一的外部訪問點
- **必須**確保聚合內的一致性
- **應該**發布領域事件

### 3.3 端口定義規範

#### 3.3.1 入站端口（Inbound Port）
- **必須**定義為介面
- **必須**以UseCase結尾命名
- **應該**使用命令/查詢分離（CQRS）

```java
public interface PlaceOrderUseCase {
    OrderId execute(PlaceOrderCommand command);
}
```

#### 3.3.2 出站端口（Outbound Port）
- **必須**定義為介面
- **必須**使用領域語言命名
- **必須不**包含技術實作細節

```java
public interface OrderRepository {
    Optional<Order> findById(OrderId id);
    Order save(Order order);
}
```

## 4. Application Layer 規範

### 4.1 套件結構
```
application/
├── service/           # 應用服務
├── dto/              # 資料傳輸物件
│   ├── command/      # 命令物件
│   ├── query/        # 查詢物件
│   └── response/     # 回應物件
├── mapper/           # 物件映射器
└── validator/        # 業務驗證器
```

### 4.2 應用服務規範
- **必須**實作領域層定義的UseCase介面
- **必須**處理事務管理
- **應該**協調多個領域服務
- **必須不**包含業務邏輯

```java
@Service
@Transactional
@RequiredArgsConstructor
public class OrderApplicationService implements PlaceOrderUseCase {
    private final OrderRepository orderRepository;
    private final PaymentPort paymentPort;
    
    @Override
    public OrderId execute(PlaceOrderCommand command) {
        // 協調領域服務，不包含業務邏輯
    }
}
```

### 4.3 DTO規範
- **必須**使用不可變物件或提供Builder模式
- **應該**包含驗證註解
- **必須不**包含業務邏輯

```java
@Value
@Builder
public class PlaceOrderCommand {
    @NotNull CustomerId customerId;
    @NotEmpty List<OrderItem> items;
    @NotNull PaymentMethod paymentMethod;
}
```

## 5. Infrastructure Layer 規範

### 5.1 套件結構
```
infrastructure/
├── adapter/
│   ├── in/            # 入站適配器
│   │   ├── web/       # REST控制器
│   │   ├── messaging/ # 訊息監聽器
│   │   └── cli/       # 命令列介面
│   └── out/           # 出站適配器
│       ├── persistence/   # 持久化
│       ├── messaging/     # 訊息發送
│       └── external/      # 外部服務
├── config/            # 配置類
└── common/            # 共用工具
```

### 5.2 適配器規範

#### 5.2.1 Web適配器
- **必須**只負責HTTP協議轉換
- **必須**進行輸入驗證
- **應該**統一異常處理

```java
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final PlaceOrderUseCase placeOrderUseCase;
    
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestBody @Valid PlaceOrderRequest request) {
        // 只做協議轉換
    }
}
```

#### 5.2.2 持久化適配器
- **必須**實作領域層定義的Repository介面
- **應該**使用獨立的Entity類別
- **必須**處理領域模型與持久化模型的轉換

```java
@Repository
@RequiredArgsConstructor
public class JpaOrderRepository implements OrderRepository {
    private final SpringDataOrderRepository jpaRepository;
    private final OrderMapper mapper;
    
    @Override
    public Order save(Order order) {
        OrderEntity entity = mapper.toEntity(order);
        entity = jpaRepository.save(entity);
        return mapper.toDomain(entity);
    }
}
```

## 6. 測試規範

### 6.1 測試策略
- Domain層：**必須**使用單元測試，無需Spring容器
- Application層：**應該**使用整合測試，Mock外部依賴
- Infrastructure層：**應該**使用整合測試，使用測試容器

### 6.2 測試覆蓋率要求
- Domain層：**必須** ≥ 90%
- Application層：**應該** ≥ 80%
- Infrastructure層：**應該** ≥ 70%

### 6.3 測試命名規範
```java
@Test
void should_ThrowException_When_OrderIsAlreadyConfirmed() {
    // Given - 前置條件
    // When - 執行動作
    // Then - 驗證結果
}
```

## 7. 通用規範

### 7.1 命名規範
| 元素 | 規範 | 範例 |
|------|------|------|
| UseCase介面 | 動詞+名詞+UseCase | PlaceOrderUseCase |
| Repository介面 | 名詞+Repository | OrderRepository |
| 領域服務 | 名詞+Service | PricingService |
| 應用服務 | 名詞+ApplicationService | OrderApplicationService |
| DTO | 用途+DTO/Command/Query | PlaceOrderCommand |
| 值物件 | 名詞 | Money, OrderId |

### 7.2 異常處理規範
- Domain層：**必須**拋出領域異常
- Application層：**可以**捕獲並轉換異常
- Infrastructure層：**必須**統一處理異常回應

```java
// Domain層
public class OrderDomainException extends RuntimeException {
    public OrderDomainException(String message) {
        super(message);
    }
}

// Infrastructure層
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(OrderDomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            OrderDomainException ex) {
        return ResponseEntity.badRequest()
            .body(ErrorResponse.of(ex.getMessage()));
    }
}
```

### 7.3 配置管理規範
- **必須**使用Spring配置類別管理Bean
- **應該**使用@ConfigurationProperties管理配置
- **必須不**在領域層使用Spring註解

## 8. 程式碼審查檢查清單

### 8.1 架構合規性
- [ ] 層次依賴方向正確
- [ ] 領域層無框架依賴
- [ ] 使用端口進行跨層通訊
- [ ] 套件結構符合規範

### 8.2 領域設計
- [ ] 實體包含業務行為
- [ ] 值物件不可變
- [ ] 聚合根維護一致性
- [ ] 使用領域語言

### 8.3 測試完整性
- [ ] 單元測試覆蓋核心業務
- [ ] 整合測試驗證流程
- [ ] 測試可獨立執行
- [ ] 測試命名清晰

## 9. 遷移指南

### 9.1 新專案
1. 使用專案模板初始化
2. 定義領域模型
3. 設計用例介面
4. 實作應用服務
5. 開發適配器

### 9.2 既有專案
1. 識別領域邊界
2. 抽取領域模型
3. 定義端口介面
4. 重構為六角形架構
5. 逐步遷移功能

## 10. 版本歷史

| 版本 | 日期 | 修改內容 | 作者 |
|------|------|----------|------|
| 1.0 | 2024-01-20 | 初始版本 | 架構團隊 |

## 附錄

### A. 專案模板
專案模板位置：`https://github.com/company/hexagonal-template`

### B. 工具支援
- ArchUnit：架構合規性檢查
- ModelMapper：物件映射
- Lombok：減少樣板程式碼

### C. 參考資源
- Hexagonal Architecture - Alistair Cockburn
- Domain-Driven Design - Eric Evans
- Clean Architecture - Robert C. Martin