# Step Definitions for Exchange Rate API
# 匯率API的步驟定義

require 'httparty'
require 'json'

# ==================== Given Steps ====================

Given('系統已啟動且API服務正常運作') do
  response = HTTParty.get("#{API_BASE_URL}/health")
  expect(response.code).to eq(200)
end

Given('資料庫連線正常') do
  # 檢查資料庫連線
  expect(ActiveRecord::Base.connected?).to be true if defined?(ActiveRecord)
end

Given('我有管理者權限') do
  @auth_token = generate_admin_token
  @request_headers['Authorization'] = "Bearer #{@auth_token}"
end

Given('我沒有管理者權限') do
  @auth_token = generate_user_token
  @request_headers['Authorization'] = "Bearer #{@auth_token}"
end

Given('我沒有登入系統') do
  @auth_token = nil
  @request_headers.delete('Authorization')
end

Given('資料庫已存在 {string} 到 {string} 的匯率為 {float}') do |from, to, rate|
  create_exchange_rate(from_currency: from, to_currency: to, rate: rate)
end

Given('資料庫沒有 {string} 到 {string} 的匯率資料') do |from, to|
  delete_exchange_rate(from_currency: from, to_currency: to)
end

Given('資料庫存在以下匯率資料:') do |table|
  table.hashes.each do |row|
    create_exchange_rate(
      from_currency: row['from_currency'],
      to_currency: row['to_currency'],
      rate: row['rate'].to_f,
      updated_at: row['updated_at']
    )
  end
end

Given('資料庫存在{int}筆匯率資料') do |count|
  count.times do |i|
    create_exchange_rate(
      from_currency: "CUR#{i}",
      to_currency: "TWD",
      rate: (i + 1) * 10.5
    )
  end
end

Given('我在{int}分鐘內已經發送了{int}次請求') do |minutes, count|
  # 模擬達到rate limit
  @rate_limit_reached = true
  @request_headers['X-Test-Rate-Limit'] = count.to_s
end

# ==================== When Steps ====================

When('我發送POST請求到 {string} 包含以下資料:') do |endpoint, table|
  data = table.hashes.first
  @response = HTTParty.post(
    "#{API_BASE_URL}#{endpoint}",
    headers: @request_headers,
    body: data.to_json
  )
end

When('我發送POST請求到 {string} 包含:') do |endpoint, json_string|
  @response = HTTParty.post(
    "#{API_BASE_URL}#{endpoint}",
    headers: @request_headers,
    body: json_string
  )
end

When('我發送GET請求到 {string}') do |endpoint|
  @response = HTTParty.get(
    "#{API_BASE_URL}#{endpoint}",
    headers: @request_headers
  )
end

When('我發送PUT請求到 {string} 包含:') do |endpoint, json_string|
  @response = HTTParty.put(
    "#{API_BASE_URL}#{endpoint}",
    headers: @request_headers,
    body: json_string
  )
end

When('我發送DELETE請求到 {string}') do |endpoint|
  @response = HTTParty.delete(
    "#{API_BASE_URL}#{endpoint}",
    headers: @request_headers
  )
end

When('我發送POST請求到 {string} 包含匯率資料') do |endpoint|
  data = {
    from_currency: 'USD',
    to_currency: 'TWD',
    rate: 32.5
  }
  @response = HTTParty.post(
    "#{API_BASE_URL}#{endpoint}",
    headers: @request_headers,
    body: data.to_json
  )
end

When('我發送第{int}次GET請求到 {string}') do |count, endpoint|
  @response = HTTParty.get(
    "#{API_BASE_URL}#{endpoint}",
    headers: @request_headers.merge('X-Request-Count' => count.to_s)
  )
end

# ==================== Then Steps ====================

Then('回應狀態碼應該是 {int}') do |expected_status|
  expect(@response.code).to eq(expected_status)
end

Then('回應應該包含新建立的匯率資料') do
  body = JSON.parse(@response.body)
  expect(body).to have_key('id')
  expect(body).to have_key('from_currency')
  expect(body).to have_key('to_currency')
  expect(body).to have_key('rate')
  expect(body).to have_key('created_at')
end

Then('資料庫應該儲存這筆匯率記錄') do
  body = JSON.parse(@response.body)
  rate = find_exchange_rate(
    from_currency: body['from_currency'],
    to_currency: body['to_currency']
  )
  expect(rate).not_to be_nil
  expect(rate['rate']).to eq(body['rate'])
end

Then('回應應該包含錯誤訊息 {string}') do |error_message|
  body = JSON.parse(@response.body)
  expect(body['error'] || body['message']).to include(error_message)
end

Then('回應應該包含:') do |expected_json|
  expected = JSON.parse(expected_json)
  actual = JSON.parse(@response.body)
  
  expected.each do |key, value|
    expect(actual[key]).to eq(value)
  end
end

Then('回應應該包含{int}筆匯率資料') do |expected_count|
  body = JSON.parse(@response.body)
  data = body['data'] || body
  expect(data).to be_an(Array)
  expect(data.length).to eq(expected_count)
end

Then('所有資料的來源貨幣都應該是 {string}') do |currency|
  body = JSON.parse(@response.body)
  data = body['data'] || body
  data.each do |rate|
    expect(rate['from_currency']).to eq(currency)
  end
end

Then('回應應該顯示更新後的匯率為 {float}') do |expected_rate|
  body = JSON.parse(@response.body)
  expect(body['rate']).to eq(expected_rate)
end

Then('資料庫中的匯率應該被更新為 {float}') do |expected_rate|
  # 等待一下確保資料庫更新完成
  sleep 0.1
  
  body = JSON.parse(@response.body)
  rate = find_exchange_rate(
    from_currency: body['from_currency'],
    to_currency: body['to_currency']
  )
  expect(rate['rate']).to eq(expected_rate)
end

Then('更新時間應該被記錄') do
  body = JSON.parse(@response.body)
  expect(body).to have_key('updated_at')
  
  updated_at = Time.parse(body['updated_at'])
  expect(updated_at).to be_within(5.seconds).of(Time.now)
end

Then('資料庫中不應該存在 {string} 到 {string} 的匯率資料') do |from, to|
  rate = find_exchange_rate(from_currency: from, to_currency: to)
  expect(rate).to be_nil
end

Then('回應應該顯示換算結果為 {float} TWD') do |expected_amount|
  body = JSON.parse(@response.body)
  expect(body['to_amount']).to eq(expected_amount)
  expect(body['to_currency']).to eq('TWD')
end

Then('回應應該說明使用了 {string} 的換算路徑') do |path|
  body = JSON.parse(@response.body)
  expect(body['conversion_path']).to eq(path)
end

Then('回應標頭應該包含 {string}') do |header_value|
  expect(@response.headers.to_s).to include(header_value)
end

Then('回應應該包含分頁資訊:') do |expected_json|
  expected = JSON.parse(expected_json)
  actual = JSON.parse(@response.body)
  
  pagination = actual['pagination'] || actual['meta']
  expected.each do |key, value|
    expect(pagination[key]).to eq(value)
  end
end

# ==================== Helper Methods ====================

def generate_admin_token
  # 產生管理員測試token
  'test_admin_token_123'
end

def generate_user_token
  # 產生一般使用者測試token
  'test_user_token_456'
end

def generate_test_token
  # 產生測試token
  'test_token_789'
end

def create_exchange_rate(params)
  # 在測試資料庫中建立匯率資料
  # 這裡應該根據實際的資料庫實作
  @test_exchange_rates ||= []
  @test_exchange_rates << params
end

def delete_exchange_rate(params)
  # 從測試資料庫中刪除匯率資料
  @test_exchange_rates ||= []
  @test_exchange_rates.reject! do |rate|
    rate[:from_currency] == params[:from_currency] &&
    rate[:to_currency] == params[:to_currency]
  end
end

def find_exchange_rate(params)
  # 從測試資料庫中查找匯率資料
  @test_exchange_rates ||= []
  @test_exchange_rates.find do |rate|
    rate[:from_currency] == params[:from_currency] &&
    rate[:to_currency] == params[:to_currency]
  end
end

def ensure_database_connection
  # 確保資料庫連線
  ActiveRecord::Base.connection.active? if defined?(ActiveRecord)
end