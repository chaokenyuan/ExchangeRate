Feature: 資料驗證與業務規則
  As a 系統管理者
  I want 確保輸入資料的正確性和完整性
  So that 系統能夠穩定運行並提供可靠的服務

  Background:
    Given 系統已啟動且API服務正常運作
    And 資料庫連線正常
    And 我有管理者權限

  # ==================== 匯率資料輸入驗證 ====================

  @validation @input-validation @exchange-rate
  Scenario Outline: 新增無效匯率資料應該失敗
    When 我發送POST請求到 "/api/exchange-rates" 包含以下資料:
      | from_currency   | to_currency   | rate   |
      | <from_currency> | <to_currency> | <rate> |
    Then 回應狀態碼應該是 400
    And 回應應該包含錯誤訊息 "<error_message>"

    Examples: 業務規則驗證
      | from_currency | to_currency | rate  | error_message            |
      | USD          | USD         | 1.0   | Source and target currencies cannot be the same      |
      | USD          | TWD         | 0     | Exchange rate must be greater than 0             |
      | USD          | TWD         | -32.5 | Exchange rate must be greater than 0             |

    Examples: 貨幣代碼驗證
      | from_currency | to_currency | rate | error_message            |
      | ABC          | TWD         | 32.5 | Unsupported currency code: ABC      |
      | USD          | XYZ         | 32.5 | Unsupported currency code: XYZ      |

    Examples: 必填欄位驗證
      | from_currency | to_currency | rate | error_message      |
      |              | TWD         | 32.5 | 來源貨幣為必填欄位   |
      | USD          |             | 32.5 | 目標貨幣為必填欄位   |
      | USD          | TWD         |      | 匯率為必填欄位      |

  # ==================== 更新匯率驗證 ====================

  @validation @input-validation @update-rate
  Scenario Outline: 更新為無效匯率值應該失敗
    Given 資料庫已存在 "USD" 到 "TWD" 的匯率為 32.5
    When 我發送PUT請求到 "/api/exchange-rates/USD/TWD" 包含:
      """json
      {
        "rate": <rate>
      }
      """
    Then 回應狀態碼應該是 400
    And 回應應該包含錯誤訊息 "Exchange rate must be greater than 0"

    Examples:
      | rate  |
      | 0     |
      | -33.0 |

  # ==================== 貨幣換算輸入驗證 ====================

  @validation @input-validation @conversion
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

    Examples: 業務規則驗證
      | from | to  | amount | status_code | error_message           |
      | USD  | USD | 100    | 400        | Source and target currencies cannot be the same      |
      | USD  | TWD | 0      | 400        | Amount must be greater than 0            |
      | USD  | TWD | -100   | 400        | Amount must be greater than 0            |

    Examples: 貨幣代碼驗證
      | from | to  | amount | status_code | error_message           |
      | ABC  | TWD | 100    | 400        | Unsupported currency code: ABC     |
      | USD  | XYZ | 100    | 400        | Unsupported currency code: XYZ     |

    Examples: 找不到匯率
      | from | to  | amount | status_code | error_message           |
      | EUR  | TWD | 100    | 400        | No exchange rate found for conversion          |

  # ==================== 貨幣代碼標準化 ====================

  @validation @normalization @currency-code
  Scenario: 貨幣代碼自動大寫轉換
    When 我發送POST請求到 "/api/exchange-rates" 包含以下資料:
      | from_currency | to_currency | rate |
      | usd          | twd         | 32.5 |
    Then 回應狀態碼應該是 201
    And 回應應該包含新建立的匯率資料
    And 回應中的貨幣代碼應該是大寫格式 "USD" 和 "TWD"

  # ==================== 資料完整性檢查 ====================

  @validation @data-integrity @rate-consistency
  Scenario: 匯率一致性檢查
    Given 資料庫已存在 "USD" 到 "TWD" 的匯率為 32.5
    When 我查詢相同匯率組合多次
    Then 所有查詢結果應該保持一致
    And 匯率值不應該出現精度丟失