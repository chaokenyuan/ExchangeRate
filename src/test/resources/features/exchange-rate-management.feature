Feature: 匯率資料管理
  As a 匯率管理員
  I want 能夠管理匯率資料
  So that 系統能夠提供準確的匯率資訊

  Background:
    Given 系統已啟動且API服務正常運作
    And 資料庫連線正常

  # ==================== 新增匯率資料 ====================
  
  @create @happy-path @exchange-rate-management
  Scenario: 成功新增匯率資料
    Given 我有管理者權限
    When 我發送POST請求到 "/api/exchange-rates" 包含以下資料:
      | from_currency | to_currency | rate |
      | USD          | TWD         | 32.5 |
    Then 回應狀態碼應該是 201
    And 回應應該包含新建立的匯率資料
    And 資料庫應該儲存這筆匯率記錄

  @create @business-rule @exchange-rate-management
  Scenario: 新增重複的匯率組合應該失敗
    Given 資料庫已存在 "USD" 到 "TWD" 的匯率為 32.0
    And 我有管理者權限
    When 我發送POST請求到 "/api/exchange-rates" 包含以下資料:
      | from_currency | to_currency | rate |
      | USD          | TWD         | 32.5 |
    Then 回應狀態碼應該是 409
    And 回應應該包含錯誤訊息 "匯率組合已存在"

  # ==================== 查詢匯率資料 ====================

  @read @happy-path @exchange-rate-management
  Scenario: 查詢特定匯率組合
    Given 資料庫存在以下匯率資料:
      | from_currency | to_currency | rate | updated_at          |
      | USD          | TWD         | 32.5 | 2024-01-15T10:00:00 |
      | EUR          | TWD         | 35.2 | 2024-01-15T10:00:00 |
    When 我發送GET請求到 "/api/exchange-rates/USD/TWD"
    Then 回應狀態碼應該是 200
    And 回應應該包含:
      """json
      {
        "from_currency": "USD",
        "to_currency": "TWD",
        "rate": 32.5,
        "updated_at": "2024-01-15T10:00:00"
      }
      """

  @read @happy-path @exchange-rate-management
  Scenario: 查詢所有匯率資料
    Given 資料庫存在以下匯率資料:
      | from_currency | to_currency | rate |
      | USD          | TWD         | 32.5 |
      | EUR          | TWD         | 35.2 |
      | JPY          | TWD         | 0.22 |
    When 我發送GET請求到 "/api/exchange-rates"
    Then 回應狀態碼應該是 200
    And 回應應該包含3筆匯率資料

  @read @edge-case @exchange-rate-management
  Scenario: 查詢不存在的匯率組合
    Given 資料庫沒有 "EUR" 到 "TWD" 的匯率資料
    When 我發送GET請求到 "/api/exchange-rates/EUR/TWD"
    Then 回應狀態碼應該是 404
    And 回應應該包含錯誤訊息 "找不到指定的匯率資料"

  @read @filter @exchange-rate-management
  Scenario: 使用篩選條件查詢匯率
    Given 資料庫存在以下匯率資料:
      | from_currency | to_currency | rate |
      | USD          | TWD         | 32.5 |
      | USD          | EUR         | 0.92 |
      | USD          | JPY         | 148  |
      | EUR          | TWD         | 35.2 |
    When 我發送GET請求到 "/api/exchange-rates?from=USD"
    Then 回應狀態碼應該是 200
    And 回應應該包含3筆匯率資料
    And 所有資料的來源貨幣都應該是 "USD"

  # ==================== 更新匯率資料 ====================

  @update @happy-path @exchange-rate-management
  Scenario: 成功更新匯率
    Given 資料庫已存在 "USD" 到 "TWD" 的匯率為 32.5
    And 我有管理者權限
    When 我發送PUT請求到 "/api/exchange-rates/USD/TWD" 包含:
      """json
      {
        "rate": 33.0
      }
      """
    Then 回應狀態碼應該是 200
    And 回應應該顯示更新後的匯率為 33.0
    And 資料庫中的匯率應該被更新為 33.0
    And 更新時間應該被記錄

  @update @edge-case @exchange-rate-management
  Scenario: 更新不存在的匯率應該失敗
    Given 資料庫沒有 "EUR" 到 "TWD" 的匯率資料
    And 我有管理者權限
    When 我發送PUT請求到 "/api/exchange-rates/EUR/TWD" 包含:
      """json
      {
        "rate": 41.5
      }
      """
    Then 回應狀態碼應該是 404
    And 回應應該包含錯誤訊息 "找不到指定的匯率資料"

  # ==================== 刪除匯率資料 ====================

  @delete @happy-path @exchange-rate-management
  Scenario: 成功刪除匯率資料
    Given 資料庫已存在 "USD" 到 "TWD" 的匯率為 32.5
    And 我有管理者權限
    When 我發送DELETE請求到 "/api/exchange-rates/USD/TWD"
    Then 回應狀態碼應該是 204
    And 資料庫中不應該存在 "USD" 到 "TWD" 的匯率資料

  @delete @edge-case @exchange-rate-management
  Scenario: 刪除不存在的匯率資料
    Given 資料庫沒有 "EUR" 到 "TWD" 的匯率資料
    And 我有管理者權限
    When 我發送DELETE請求到 "/api/exchange-rates/EUR/TWD"
    Then 回應狀態碼應該是 404
    And 回應應該包含錯誤訊息 "找不到指定的匯率資料"