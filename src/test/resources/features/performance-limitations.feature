Feature: 系統效能與限制
  As a 系統管理者
  I want 控制系統資源使用並提供良好的效能
  So that 系統能夠穩定運行並為所有使用者提供優質服務

  Background:
    Given 系統已啟動且API服務正常運作
    And 資料庫連線正常

  # ==================== API頻率限制 ====================

  @performance @rate-limit @throttling
  Scenario: API請求頻率限制
    Given 我在1分鐘內已經發送了100次請求
    When 我發送第101次GET請求到 "/api/exchange-rates"
    Then 回應狀態碼應該是 429
    And 回應應該包含錯誤訊息 "請求過於頻繁"
    And 回應標頭應該包含 "X-RateLimit-Remaining: 0"

  @performance @rate-limit @reset-window
  Scenario: 頻率限制時間窗口重置
    Given 我在前1分鐘內已達到請求限制
    When 限制時間窗口重置後
    And 我發送GET請求到 "/api/exchange-rates"
    Then 回應狀態碼應該是 200
    And 回應標頭應該顯示重置後的限制計數

  @performance @rate-limit @per-user
  Scenario: 按使用者分別計算頻率限制
    Given 使用者A已達到請求頻率限制
    When 使用者B發送GET請求到 "/api/exchange-rates"
    Then 回應狀態碼應該是 200
    And 使用者B不受使用者A的限制影響

  # ==================== 分頁查詢效能 ====================

  @performance @pagination @large-dataset
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

  @performance @pagination @boundary-conditions
  Scenario Outline: 分頁邊界條件測試
    Given 資料庫存在100筆匯率資料
    When 我發送GET請求到 "/api/exchange-rates?page=<page>&limit=<limit>"
    Then 回應狀態碼應該是 <status_code>
    And 回應應該包含<expected_count>筆資料

    Examples:
      | page | limit | status_code | expected_count |
      | 1    | 25    | 200        | 25            |
      | 4    | 25    | 200        | 25            |
      | 5    | 25    | 200        | 0             |
      | 0    | 25    | 400        | 0             |
      | 1    | 0     | 400        | 0             |

  # ==================== 響應時間要求 ====================

  @performance @response-time @sla
  Scenario: API響應時間應符合SLA要求
    Given 資料庫存在標準數量的匯率資料
    When 我發送GET請求到 "/api/exchange-rates"
    Then 回應時間應該小於500毫秒
    And 回應狀態碼應該是 200

  @performance @response-time @conversion
  Scenario: 貨幣換算響應時間測試
    Given 資料庫已存在 "USD" 到 "TWD" 的匯率為 32.5
    When 我發送POST請求到 "/api/convert" 包含:
      """json
      {
        "from_currency": "USD",
        "to_currency": "TWD",
        "amount": 100
      }
      """
    Then 回應時間應該小於200毫秒
    And 回應狀態碼應該是 200

  # ==================== 併發處理能力 ====================

  @performance @concurrency @concurrent-reads
  Scenario: 併發讀取請求處理
    Given 資料庫已存在 "USD" 到 "TWD" 的匯率為 32.5
    When 同時發送10個GET請求到 "/api/exchange-rates/USD/TWD"
    Then 所有請求都應該成功返回
    And 所有回應的匯率值都應該一致
    And 沒有請求應該超時或失敗

  @performance @concurrency @concurrent-writes
  Scenario: 併發寫入請求處理
    Given 我有管理者權限
    When 同時嘗試創建不同的匯率組合
    Then 所有有效的請求都應該成功
    And 不應該出現資料競爭或不一致狀態
    And 資料庫中的資料應該保持完整性

  # ==================== 資源使用限制 ====================

  @performance @resource-limit @memory-usage
  Scenario: 記憶體使用量控制
    When 處理大量併發請求
    Then 系統記憶體使用量應該保持在合理範圍內
    And 不應該出現記憶體洩漏
    And 垃圾回收應該正常運作

  @performance @resource-limit @connection-pool
  Scenario: 資料庫連線池管理
    Given 系統配置了有限的資料庫連線池
    When 併發請求數超過連線池大小
    Then 請求應該正確排隊等待
    And 所有請求最終都應該得到處理
    And 連線池不應該耗盡

  # ==================== 快取機制效能 ====================

  @performance @cache @response-caching
  Scenario: 查詢結果快取提升效能
    Given 資料庫已存在 "USD" 到 "TWD" 的匯率為 32.5
    When 我第一次發送GET請求到 "/api/exchange-rates/USD/TWD"
    Then 回應時間應該被記錄為基準時間
    When 我再次發送相同的GET請求
    Then 第二次請求的回應時間應該明顯快於第一次
    And 回應內容應該完全相同