Feature: 安全認證與權限控制
  As a 系統安全管理員
  I want 控制不同使用者對系統功能的訪問權限
  So that 確保系統安全並防止未授權操作

  Background:
    Given 系統已啟動且API服務正常運作
    And 資料庫連線正常

  # ==================== 身份認證檢查 ====================

  @security @authentication @login-required
  Scenario: 未認證使用者只能查詢不能修改
    Given 我沒有登入系統
    When 我發送GET請求到 "/api/exchange-rates"
    Then 回應狀態碼應該是 200
    When 我發送POST請求到 "/api/exchange-rates" 包含匯率資料
    Then 回應狀態碼應該是 401
    And 回應應該包含錯誤訊息 "需要登入"

  @security @authentication @token-validation
  Scenario: 無效認證令牌被拒絕
    Given 我使用過期的認證令牌
    When 我發送POST請求到 "/api/exchange-rates" 包含匯率資料
    Then 回應狀態碼應該是 401
    And 回應應該包含錯誤訊息 "認證令牌無效或已過期"

  # ==================== 權限授權檢查 ====================

  @security @authorization @admin-required
  Scenario: 無權限使用者不能修改匯率資料
    Given 我沒有管理者權限
    When 我發送POST請求到 "/api/exchange-rates" 包含匯率資料
    Then 回應狀態碼應該是 403
    And 回應應該包含錯誤訊息 "權限不足"

  @security @authorization @admin-operations
  Scenario Outline: 管理者操作權限檢查
    Given 我沒有管理者權限
    But 我有登入系統
    And 資料庫已存在 "USD" 到 "TWD" 的匯率為 32.5
    When 我發送<method>請求到 "<endpoint>"
    Then 回應狀態碼應該是 403
    And 回應應該包含錯誤訊息 "權限不足"

    Examples:
      | method | endpoint                      |
      | POST   | /api/exchange-rates          |
      | PUT    | /api/exchange-rates/USD/TWD  |
      | DELETE | /api/exchange-rates/USD/TWD  |

  # ==================== 讀取權限控制 ====================

  @security @authorization @public-read
  Scenario: 一般使用者可以進行查詢和換算操作
    Given 我沒有管理者權限
    But 我有登入系統
    And 資料庫已存在 "USD" 到 "TWD" 的匯率為 32.5
    When 我發送GET請求到 "/api/exchange-rates"
    Then 回應狀態碼應該是 200
    When 我發送POST請求到 "/api/convert" 包含:
      """json
      {
        "from_currency": "USD",
        "to_currency": "TWD",
        "amount": 100
      }
      """
    Then 回應狀態碼應該是 200

  # ==================== 角色基礎訪問控制 ====================

  @security @rbac @role-based-access
  Scenario Outline: 不同角色的操作權限
    Given 我的角色是 "<role>"
    When 我嘗試執行 "<operation>" 操作
    Then 操作結果應該是 "<result>"
    And 如果失敗，錯誤訊息應該是 "<error_message>"

    Examples: 管理者權限操作
      | role       | operation     | result  | error_message |
      | admin      | create_rate   | success |               |
      | admin      | update_rate   | success |               |
      | admin      | delete_rate   | success |               |
      
    Examples: 一般使用者權限操作  
      | role       | operation     | result  | error_message |
      | user       | read_rate     | success |               |
      | user       | convert       | success |               |
      | user       | create_rate   | failed  | 權限不足       |
      
    Examples: 匿名使用者權限操作
      | role       | operation     | result  | error_message |
      | anonymous  | read_rate     | success |               |
      | anonymous  | create_rate   | failed  | 需要登入       |

  # ==================== 安全審計日誌 ====================

  @security @audit @operation-logging
  Scenario: 敏感操作應該被記錄
    Given 我有管理者權限
    When 我發送POST請求到 "/api/exchange-rates" 包含以下資料:
      | from_currency | to_currency | rate |
      | USD          | TWD         | 32.5 |
    Then 回應狀態碼應該是 201
    And 系統應該記錄此次操作的審計日誌
    And 日誌應該包含使用者身份、操作時間和操作內容

  # ==================== 會話安全 ====================

  @security @session @timeout
  Scenario: 會話超時自動登出
    Given 我有有效的登入會話
    When 會話閒置超過設定時間
    And 我發送POST請求到 "/api/exchange-rates" 包含匯率資料
    Then 回應狀態碼應該是 401
    And 回應應該包含錯誤訊息 "會話已過期，請重新登入"