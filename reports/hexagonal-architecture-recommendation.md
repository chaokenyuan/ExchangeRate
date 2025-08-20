# Spring Boot 六角形架構建議

## 執行摘要
本文件提供基於Spring Boot框架的六角形架構（Hexagonal Architecture）實作建議，旨在建立高度模組化、可測試且易於維護的企業級應用程式。

## 架構概述

### 核心概念
六角形架構（又稱Ports & Adapters架構）將應用程式分為三個主要層次：

1. **Domain Layer（領域層）** - 業務邏輯核心
2. **Application Layer（應用層）** - 協調業務流程
3. **Infrastructure Layer（基礎設施層）** - 外部介接實作

### 架構原則
- **依賴反轉原則**：業務邏輯不依賴外部框架或技術細節
- **關注點分離**：每層只負責特定職責
- **可測試性優先**：核心業務邏輯可獨立測試
- **技術無關性**：易於更換技術棧

## 專案結構

### 建議的目錄結構
```
com.company.project/
├── domain/                    # 領域層（內核心）
│   ├── model/                # 領域模型
│   │   ├── entity/          # 實體
│   │   ├── valueobject/     # 值物件
│   │   └── aggregate/       # 聚合根
│   ├── service/              # 領域服務
│   ├── exception/            # 領域異常
│   └── port/                 # 端口定義
│       ├── in/               # 入站端口（Use Cases）
│       └── out/              # 出站端口（Repository介面）
├── application/              # 應用層
│   ├── service/              # 應用服務實作
│   ├── dto/                  # 資料傳輸物件
│   │   ├── command/          # 命令物件
│   │   └── query/            # 查詢物件
│   └── mapper/               # DTO映射器
└── infrastructure/           # 基礎設施層（外適配器）
    ├── adapter/
    │   ├── in/               # 入站適配器
    │   │   ├── web/          # REST Controllers
    │   │   ├── messaging/    # Message Listeners
    │   │   └── scheduler/    # 排程任務
    │   └── out/              # 出站適配器
    │       ├── persistence/  # JPA Repositories
    │       ├── external/     # 外部服務客戶端
    │       └── messaging/    # 訊息發送器
    ├── config/               # Spring配置類
    └── common/               # 共用元件

```

## 層次詳細設計

### 1. Domain Layer（領域層）

#### 領域模型範例
```java
// domain/model/entity/Order.java
@Entity
@Table(name = "orders")
public class Order {
    @EmbeddedId
    private OrderId id;
    
    @Embedded
    private CustomerId customerId;
    
    @Embedded
    private Money totalAmount;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    // 業務方法
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new OrderDomainException("只能確認待處理訂單");
        }
        status = OrderStatus.CONFIRMED;
        registerEvent(new OrderConfirmedEvent(this.id));
    }
    
    public void addItem(Product product, int quantity) {
        OrderItem item = new OrderItem(product, quantity);
        items.add(item);
        recalculateTotal();
    }
    
    private void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }
}

// domain/model/valueobject/OrderId.java
@Embeddable
public class OrderId implements Serializable {
    @Column(name = "order_id")
    private String value;
    
    protected OrderId() {} // JPA需要
    
    public OrderId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("OrderId不能為空");
        }
        this.value = value;
    }
    
    public static OrderId generate() {
        return new OrderId(UUID.randomUUID().toString());
    }
    
    // equals, hashCode, toString...
}
```

#### 端口定義
```java
// domain/port/in/PlaceOrderUseCase.java
public interface PlaceOrderUseCase {
    OrderId placeOrder(PlaceOrderCommand command);
}

// domain/port/in/QueryOrderUseCase.java
public interface QueryOrderUseCase {
    OrderDTO findById(OrderId orderId);
    Page<OrderDTO> findByCustomer(CustomerId customerId, Pageable pageable);
}

// domain/port/out/OrderRepository.java
public interface OrderRepository {
    Optional<Order> findById(OrderId id);
    Order save(Order order);
    void delete(Order order);
    Page<Order> findByCustomerId(CustomerId customerId, Pageable pageable);
}

// domain/port/out/PaymentPort.java
public interface PaymentPort {
    PaymentResult processPayment(Money amount, PaymentMethod method);
    void refundPayment(PaymentId paymentId);
}
```

### 2. Application Layer（應用層）

#### 應用服務實作
```java
// application/service/OrderService.java
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService implements PlaceOrderUseCase, QueryOrderUseCase {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PaymentPort paymentPort;
    private final NotificationPort notificationPort;
    private final OrderMapper orderMapper;
    
    @Override
    public OrderId placeOrder(PlaceOrderCommand command) {
        // 1. 驗證商品
        List<Product> products = validateProducts(command.getItems());
        
        // 2. 建立訂單
        Order order = Order.create(command.getCustomerId());
        command.getItems().forEach(item -> {
            Product product = findProduct(products, item.getProductId());
            order.addItem(product, item.getQuantity());
        });
        
        // 3. 處理付款
        PaymentResult payment = paymentPort.processPayment(
            order.getTotalAmount(), 
            command.getPaymentMethod()
        );
        
        if (!payment.isSuccessful()) {
            throw new PaymentFailedException(payment.getErrorMessage());
        }
        
        order.confirmPayment(payment.getPaymentId());
        
        // 4. 儲存訂單
        order = orderRepository.save(order);
        
        // 5. 發送通知（非同步）
        notificationPort.sendOrderConfirmation(order);
        
        return order.getId();
    }
    
    @Override
    @Transactional(readOnly = true)
    public OrderDTO findById(OrderId orderId) {
        return orderRepository.findById(orderId)
            .map(orderMapper::toDTO)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
```

#### DTO定義
```java
// application/dto/command/PlaceOrderCommand.java
@Data
@Builder
@Validated
public class PlaceOrderCommand {
    @NotNull
    private CustomerId customerId;
    
    @NotEmpty
    private List<OrderItemCommand> items;
    
    @NotNull
    private PaymentMethod paymentMethod;
    
    @Data
    public static class OrderItemCommand {
        @NotNull
        private ProductId productId;
        
        @Min(1)
        private int quantity;
    }
}

// application/dto/OrderDTO.java
@Data
@Builder
public class OrderDTO {
    private String orderId;
    private String customerId;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;
}
```

### 3. Infrastructure Layer（基礎設施層）

#### 入站適配器
```java
// infrastructure/adapter/in/web/OrderController.java
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "訂單管理", description = "訂單相關操作")
public class OrderController {
    private final PlaceOrderUseCase placeOrderUseCase;
    private final QueryOrderUseCase queryOrderUseCase;
    
    @PostMapping
    @Operation(summary = "建立訂單")
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestBody @Valid PlaceOrderRequest request) {
        PlaceOrderCommand command = OrderRequestMapper.toCommand(request);
        OrderId orderId = placeOrderUseCase.placeOrder(command);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(orderId.getValue())
            .toUri();
            
        return ResponseEntity.created(location)
            .body(new OrderResponse(orderId.getValue()));
    }
    
    @GetMapping("/{orderId}")
    @Operation(summary = "查詢訂單")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable String orderId) {
        OrderDTO order = queryOrderUseCase.findById(new OrderId(orderId));
        return ResponseEntity.ok(order);
    }
}
```

#### 出站適配器
```java
// infrastructure/adapter/out/persistence/JpaOrderRepository.java
@Repository
@RequiredArgsConstructor
public class JpaOrderRepository implements OrderRepository {
    private final SpringDataOrderRepository springRepo;
    private final OrderEntityMapper mapper;
    
    @Override
    public Optional<Order> findById(OrderId id) {
        return springRepo.findById(id.getValue())
            .map(mapper::toDomain);
    }
    
    @Override
    public Order save(Order order) {
        OrderEntity entity = mapper.toEntity(order);
        entity = springRepo.save(entity);
        return mapper.toDomain(entity);
    }
}

// infrastructure/adapter/out/persistence/SpringDataOrderRepository.java
@Repository
public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, String> {
    Page<OrderEntity> findByCustomerId(String customerId, Pageable pageable);
    
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status AND o.createdAt < :before")
    List<OrderEntity> findExpiredOrders(@Param("status") String status, 
                                       @Param("before") LocalDateTime before);
}
```

## 配置與整合

### Spring Boot配置
```java
// infrastructure/config/ApplicationConfig.java
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@ComponentScan(basePackages = {
    "com.company.project.application",
    "com.company.project.infrastructure"
})
public class ApplicationConfig {
    
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper;
    }
    
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}

// infrastructure/config/SecurityConfig.java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/orders/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .build();
    }
}
```

## 測試策略

### 單元測試（Domain Layer）
```java
@Test
void shouldConfirmPendingOrder() {
    // Given
    Order order = Order.create(new CustomerId("CUST-001"));
    order.addItem(createProduct(), 2);
    
    // When
    order.confirm();
    
    // Then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    assertThat(order.getEvents()).hasSize(1);
}
```

### 整合測試（Application Layer）
```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderServiceIntegrationTest {
    
    @Test
    void shouldPlaceOrderSuccessfully() {
        // Given
        PlaceOrderCommand command = createValidCommand();
        when(paymentPort.processPayment(any(), any()))
            .thenReturn(PaymentResult.success("PAY-123"));
        
        // When
        OrderId orderId = orderService.placeOrder(command);
        
        // Then
        assertThat(orderId).isNotNull();
        verify(notificationPort).sendOrderConfirmation(any());
    }
}
```

### API測試（Infrastructure Layer）
```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    
    @Test
    void shouldCreateOrder() throws Exception {
        // Given
        PlaceOrderRequest request = createValidRequest();
        when(placeOrderUseCase.placeOrder(any()))
            .thenReturn(OrderId.generate());
        
        // When & Then
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"));
    }
}
```

## 最佳實踐

### 1. 領域層設計
- 使用值物件封裝業務概念
- 聚合根負責維護一致性
- 領域事件記錄重要業務動作
- 避免貧血模型

### 2. 依賴管理
- 使用建構子注入
- 介面定義在領域層
- 實作在基礎設施層
- 避免循環依賴

### 3. 異常處理
```java
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

### 4. 效能優化
- 使用投影查詢減少資料傳輸
- 實作快取策略
- 非同步處理非關鍵業務
- 資料庫查詢優化

## 結論
六角形架構為Spring Boot應用提供了清晰的組織結構，透過明確的層次劃分和依賴管理，實現了高內聚低耦合的設計目標。這種架構特別適合需要長期維護和演進的企業級應用。