# 六角形架構遷移計劃

## 當前架構分析
- 傳統Spring Boot分層架構
- 業務邏輯集中在Service層
- 缺乏清晰的領域邊界
- 存在框架耦合問題

## 目標六角形架構

```
com.exchangerate/
├── domain/                     # 領域層（核心）
│   ├── model/                  # 領域模型
│   │   ├── entity/            # 實體
│   │   ├── valueobject/       # 值物件
│   │   └── aggregate/         # 聚合根
│   ├── service/                # 領域服務
│   ├── exception/              # 領域異常
│   └── port/                   # 端口定義
│       ├── in/                 # 入站端口（Use Cases）
│       └── out/                # 出站端口（Repository介面）
├── application/                # 應用層
│   ├── service/                # 應用服務實作
│   ├── dto/                    # 資料傳輸物件
│   │   ├── command/            # 命令物件
│   │   ├── query/              # 查詢物件
│   │   └── response/           # 回應物件
│   └── mapper/                 # DTO映射器
└── infrastructure/             # 基礎設施層
    ├── adapter/
    │   ├── in/                 # 入站適配器
    │   │   └── web/            # REST Controllers
    │   └── out/                # 出站適配器
    │       └── persistence/    # JPA Repositories
    ├── config/                 # 配置類
    └── common/                 # 共用工具
```

## 遷移步驟

1. **Domain層設計** (TDD驅動)
   - 識別核心領域概念
   - 設計領域模型和值物件
   - 定義端口介面
   - 實現領域服務

2. **Application層實作**
   - 實作Use Case介面
   - 設計DTO和映射器
   - 事務管理協調

3. **Infrastructure層重構**
   - 重構現有Controller為Web Adapter
   - 重構Repository為Persistence Adapter
   - 配置依賴注入

4. **測試遷移**
   - 領域層單元測試
   - 應用層整合測試
   - 基礎設施層端到端測試

## 核心業務概念識別

### 主要實體
- **ExchangeRate**: 匯率實體（聚合根）
- **CurrencyPair**: 貨幣對值物件
- **Rate**: 匯率值物件
- **ConversionResult**: 轉換結果值物件

### 核心用例
- 查詢匯率
- 創建/更新匯率
- 貨幣轉換
- 匯率歷史查詢

### 領域服務
- 匯率計算服務
- 貨幣轉換服務
- 匯率驗證服務