Feature: 貨幣換算服務
  As a 需要進行貨幣換算的使用者
  I want 能夠進行各種貨幣換算
  So that 我可以快速準確地計算不同貨幣間的價值

  Background:
    Given 系統已啟動且API服務正常運作
    And 資料庫連線正常

  # ==================== 直接貨幣換算 ====================

  @conversion @happy-path @direct-conversion
  Scenario: 成功進行直接貨幣換算
    Given 資料庫已存在 "USD" 到 "TWD" 的匯率為 32.5
    When 我發送POST請求到 "/api/convert" 包含:
      """json
      {
        "from_currency": "USD",
        "to_currency": "TWD",
        "amount": 100
      }
      """
    Then 回應狀態碼應該是 200
    And 回應應該包含:
      """json
      {
        "from_currency": "USD",
        "to_currency": "TWD",
        "from_amount": 100,
        "to_amount": 3250,
        "rate": 32.5,
        "conversion_date": "2024-01-15T10:00:00"
      }
      """

  # ==================== 反向貨幣換算 ====================

  @conversion @edge-case @reverse-conversion
  Scenario: 進行反向貨幣換算
    Given 資料庫已存在 "USD" 到 "TWD" 的匯率為 32.5
    But 資料庫沒有 "TWD" 到 "USD" 的匯率資料
    When 我發送POST請求到 "/api/convert" 包含:
      """json
      {
        "from_currency": "TWD",
        "to_currency": "USD",
        "amount": 3250
      }
      """
    Then 回應狀態碼應該是 200
    And 回應應該包含:
      """json
      {
        "from_currency": "TWD",
        "to_currency": "USD",
        "from_amount": 3250,
        "to_amount": 100,
        "rate": 0.03077,
        "conversion_date": "2024-01-15T10:00:00"
      }
      """

  # ==================== 鏈式貨幣換算 ====================

  @conversion @chain @multi-step-conversion
  Scenario: 透過中介貨幣進行換算
    Given 資料庫存在以下匯率資料:
      | from_currency | to_currency | rate |
      | USD          | TWD         | 32.5 |
      | EUR          | USD         | 1.08 |
    But 資料庫沒有 "EUR" 到 "TWD" 的直接匯率
    When 我發送POST請求到 "/api/convert" 包含:
      """json
      {
        "from_currency": "EUR",
        "to_currency": "TWD",
        "amount": 100
      }
      """
    Then 回應狀態碼應該是 200
    And 回應應該顯示換算結果為 3510 TWD
    And 回應應該說明使用了 "EUR→USD→TWD" 的換算路徑
    And 換算路徑資訊應該包含中介匯率詳情

  # ==================== 換算錯誤處理 ====================

  @conversion @edge-case @no-rate-available
  Scenario: 無可用匯率進行換算
    Given 資料庫沒有 "XXX" 到 "YYY" 的匯率資料
    When 我發送POST請求到 "/api/convert" 包含:
      """json
      {
        "from_currency": "XXX",
        "to_currency": "YYY",
        "amount": 100
      }
      """
    Then 回應狀態碼應該是 400
    And 回應應該包含錯誤訊息 "不支援的貨幣代碼: XXX"

  # ==================== 換算精度計算 ====================

  @conversion @precision @decimal-handling
  Scenario: 高精度小數換算
    Given 資料庫已存在 "USD" 到 "JPY" 的匯率為 148.123456
    When 我發送POST請求到 "/api/convert" 包含:
      """json
      {
        "from_currency": "USD",
        "to_currency": "JPY",
        "amount": 1.5
      }
      """
    Then 回應狀態碼應該是 200
    And 換算結果應該保持適當的精度
    And 回應中的to_amount應該是整數 222

  @conversion @rounding @currency-specific
  Scenario Outline: 不同貨幣的四捨五入規則
    Given 資料庫已存在 "USD" 到 "<target_currency>" 的匯率為 <rate>
    When 我發送POST請求到 "/api/convert" 包含:
      """json
      {
        "from_currency": "USD",
        "to_currency": "<target_currency>",
        "amount": 1
      }
      """
    Then 回應狀態碼應該是 200
    And 回應中的to_amount應該符合<target_currency>的精度規則

    Examples:
      | target_currency | rate      |
      | TWD            | 32.123    |
      | JPY            | 148.567   |
      | CNY            | 7.234567  |