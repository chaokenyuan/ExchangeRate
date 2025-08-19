Feature: 匯率換算API
  As a 需要進行貨幣換算的使用者
  I want 有一個匯率管理與換算的API
  So that 我能夠管理匯率資料並進行即時貨幣換算

  Background:
    Given 系統已啟動且API服務正常運作
    And 資料庫連線正常

  # ==================== 新增匯率資料 ====================
  
  @create @happy-path
  Scenario: 成功新增匯率資料
    Given 我有管理者權限
    When 我發送POST請求到 "/api/exchange-rates" 包含以下資料:
      | from_currency | to_currency | rate |
      | USD          | TWD         | 32.5 |
    Then 回應狀態碼應該是 201
    And 回應應該包含新建立的匯率資料
    And 資料庫應該儲存這筆匯率記錄

  @create @validation
  Scenario: 新增重複的匯率組合應該失敗
    Given 資料庫已存在 "USD" 到 "TWD" 的匯率為 32.0
    When 我發送POST請求到 "/api/exchange-rates" 包含以下資料:
      | from_currency | to_currency | rate |
      | USD          | TWD         | 32.5 |
    Then 回應狀態碼應該是 409
    And 回應應該包含錯誤訊息 "匯率組合已存在"

  @create @validation
  Scenario Outline: 新增無效匯率資料應該失敗
    When 我發送POST請求到 "/api/exchange-rates" 包含以下資料:
      | from_currency   | to_currency   | rate   |
      | <from_currency> | <to_currency> | <rate> |
    Then 回應狀態碼應該是 400
    And 回應應該包含錯誤訊息 "<error_message>"

    Examples:
      | from_currency | to_currency | rate  | error_message            |
      | USD          | USD         | 1.0   | 來源與目標貨幣不可相同      |
      | USD          | TWD         | 0     | 匯率必須大於0             |
      | USD          | TWD         | -32.5 | 匯率必須大於0             |
      | ABC          | TWD         | 32.5  | 不支援的貨幣代碼: ABC      |
      | USD          | XYZ         | 32.5  | 不支援的貨幣代碼: XYZ      |
      |              | TWD         | 32.5  | 來源貨幣為必填欄位         |
      | USD          |             | 32.5  | 目標貨幣為必填欄位         |
      | USD          | TWD         |       | 匯率為必填欄位            |

  # ==================== 查詢匯率資料 ====================

  @read @happy-path
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

  @read @happy-path
  Scenario: 查詢所有匯率資料
    Given 資料庫存在以下匯率資料:
      | from_currency | to_currency | rate |
      | USD          | TWD         | 32.5 |
      | EUR          | TWD         | 35.2 |
      | JPY          | TWD         | 0.22 |
    When 我發送GET請求到 "/api/exchange-rates"
    Then 回應狀態碼應該是 200
    And 回應應該包含3筆匯率資料

  @read @edge-case
  Scenario: 查詢不存在的匯率組合
    Given 資料庫沒有 "GBP" 到 "TWD" 的匯率資料
    When 我發送GET請求到 "/api/exchange-rates/GBP/TWD"
    Then 回應狀態碼應該是 404
    And 回應應該包含錯誤訊息 "找不到指定的匯率資料"

  @read @filter
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

  @update @happy-path
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

  @update @validation
  Scenario: 更新不存在的匯率應該失敗
    Given 資料庫沒有 "GBP" 到 "TWD" 的匯率資料
    When 我發送PUT請求到 "/api/exchange-rates/GBP/TWD" 包含:
      """json
      {
        "rate": 41.5
      }
      """
    Then 回應狀態碼應該是 404
    And 回應應該包含錯誤訊息 "找不到指定的匯率資料"

  @update @validation
  Scenario Outline: 更新為無效匯率值應該失敗
    Given 資料庫已存在 "USD" 到 "TWD" 的匯率為 32.5
    When 我發送PUT請求到 "/api/exchange-rates/USD/TWD" 包含:
      """json
      {
        "rate": <rate>
      }
      """
    Then 回應狀態碼應該是 400
    And 回應應該包含錯誤訊息 "匯率必須大於0"

    Examples:
      | rate  |
      | 0     |
      | -33.0 |

  # ==================== 刪除匯率資料 ====================

  @delete @happy-path
  Scenario: 成功刪除匯率資料
    Given 資料庫已存在 "USD" 到 "TWD" 的匯率為 32.5
    And 我有管理者權限
    When 我發送DELETE請求到 "/api/exchange-rates/USD/TWD"
    Then 回應狀態碼應該是 204
    And 資料庫中不應該存在 "USD" 到 "TWD" 的匯率資料

  @delete @edge-case
  Scenario: 刪除不存在的匯率資料
    Given 資料庫沒有 "GBP" 到 "TWD" 的匯率資料
    When 我發送DELETE請求到 "/api/exchange-rates/GBP/TWD"
    Then 回應狀態碼應該是 404
    And 回應應該包含錯誤訊息 "找不到指定的匯率資料"

  # ==================== 貨幣換算功能 ====================

  @conversion @happy-path
  Scenario: 成功進行貨幣換算
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

  @conversion @edge-case
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

  @conversion @chain
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

  @conversion @validation
  Scenario Outline: 無效的換算請求應該失敗
    When 我發送POST請求到 "/api/convert" 包含:
      """json
      {
        "from_currency": "<from>",
        "to_currency": "<to>",
        "amount": <amount>
      }
      """
    Then 回應狀態碼應該是 <status_code>
    And 回應應該包含錯誤訊息 "<error_message>"

    Examples:
      | from | to  | amount | status_code | error_message           |
      | USD  | USD | 100    | 400        | 來源與目標貨幣不可相同      |
      | USD  | TWD | 0      | 400        | 金額必須大於0            |
      | USD  | TWD | -100   | 400        | 金額必須大於0            |
      | ABC  | TWD | 100    | 400        | 不支援的貨幣代碼: ABC     |
      | USD  | XYZ | 100    | 400        | 不支援的貨幣代碼: XYZ     |
      | GBP  | TWD | 100    | 400        | 不支援的貨幣代碼: GBP     |

  # ==================== 權限控制 ====================

  @security @authorization
  Scenario: 無權限使用者不能修改匯率資料
    Given 我沒有管理者權限
    When 我發送POST請求到 "/api/exchange-rates" 包含匯率資料
    Then 回應狀態碼應該是 403
    And 回應應該包含錯誤訊息 "權限不足"

  @security @authentication
  Scenario: 未認證使用者只能查詢不能修改
    Given 我沒有登入系統
    When 我發送GET請求到 "/api/exchange-rates"
    Then 回應狀態碼應該是 200
    When 我發送POST請求到 "/api/exchange-rates" 包含匯率資料
    Then 回應狀態碼應該是 401
    And 回應應該包含錯誤訊息 "需要登入"

  # ==================== 效能與限制 ====================

  @performance @rate-limit
  Scenario: API請求頻率限制
    Given 我在1分鐘內已經發送了100次請求
    When 我發送第101次GET請求到 "/api/exchange-rates"
    Then 回應狀態碼應該是 429
    And 回應應該包含錯誤訊息 "請求過於頻繁"
    And 回應標頭應該包含 "X-RateLimit-Remaining: 0"

  @performance @pagination
  Scenario: 大量資料分頁查詢
    Given 資料庫存在150筆匯率資料
    When 我發送GET請求到 "/api/exchange-rates?page=1&limit=50"
    Then 回應狀態碼應該是 200
    And 回應應該包含50筆資料
    And 回應應該包含分頁資訊:
      """json
      {
        "current_page": 1,
        "total_pages": 3,
        "total_records": 150,
        "has_next": true
      }
      """