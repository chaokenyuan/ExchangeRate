Feature: 系統整合基礎功能
  As a 系統管理者
  I want 確保系統基礎功能正常運作
  So that 其他業務功能能夠穩定運行

  Background:
    Given 系統已啟動且API服務正常運作
    And 資料庫連線正常

  @smoke @integration
  Scenario: 系統健康檢查
    When 我發送GET請求到 "/api/exchange-rates"
    Then 回應狀態碼應該是 200

  @smoke @integration
  Scenario: 資料庫連線測試
    Given 資料庫已存在 "USD" 到 "TWD" 的匯率為 32.5
    When 我發送GET請求到 "/api/exchange-rates/USD/TWD"
    Then 回應狀態碼應該是 200
    And 回應應該包含:
      """json
      {
        "from_currency": "USD",
        "to_currency": "TWD",
        "rate": 32.5
      }
      """