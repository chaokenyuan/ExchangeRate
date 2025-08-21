# 技術棧配置 (Tech Stack Configuration)

## 概述

技術棧配置定義了專案可使用的技術工具範圍，確保團隊成員在統一的技術環境下工作，避免技術選型混亂。

## 🛠️ 可用技術棧

### 1️⃣ Spring Boot (Java)
```yaml
stack_id: "springboot"
name: "Spring Boot"
language: "Java"
version: "3.2.0"
status: "active"
description: "Java企業級應用開發框架"

framework:
  core: "Spring Boot 3.2.0"
  language_version: "Java 17+"
  build_tool: "Maven 3.9+"

dependencies:
  web: "spring-boot-starter-web"
  data: "spring-boot-starter-data-jpa"
  validation: "spring-boot-starter-validation"
  test: "spring-boot-starter-test"
  security: "spring-boot-starter-security"

database:
  development: "H2 Database"
  production: "MySQL/PostgreSQL"

testing:
  unit: "JUnit 5"
  integration: "Spring Boot Test"
  bdd: "Cucumber-JVM"
  api: "REST Assured"

tools:
  containerization: "Docker"
  documentation: "Swagger/OpenAPI 3"
  monitoring: "Spring Boot Actuator"

allowed_libraries:
  - "Jackson (JSON處理)"
  - "Lombok (程式碼簡化)"
  - "MapStruct (物件映射)"
  - "Apache Commons"
  - "Hibernate Validator"
  - "TestContainers (測試容器)"

coding_conventions:
  - "Google Java Style Guide"
  - "Spring Boot最佳實踐"
  - "RESTful API設計原則"
```

### 2️⃣ Node.js (Express)
```yaml
stack_id: "nodejs"
name: "Node.js Express"
language: "JavaScript/TypeScript"
version: "20.x"
status: "available"
description: "JavaScript後端應用開發"

framework:
  core: "Express.js 4.x"
  language_version: "Node.js 20.x"
  build_tool: "npm/yarn"

dependencies:
  web: "express"
  database: "mongoose/sequelize"
  validation: "joi/express-validator"
  test: "jest/mocha"

database:
  development: "SQLite/MongoDB"
  production: "MongoDB/PostgreSQL"

testing:
  unit: "Jest"
  integration: "Supertest"
  bdd: "Cucumber-JS"

allowed_libraries:
  - "Lodash (工具函式)"
  - "Axios (HTTP客戶端)"
  - "Bcrypt (密碼加密)"
  - "JWT (認證)"
  - "Winston (日誌)"
```

### 3️⃣ Python (Django/FastAPI)
```yaml
stack_id: "python"
name: "Python Web"
language: "Python"
version: "3.11+"
status: "available"
description: "Python Web應用開發"

framework:
  core: "Django 4.x / FastAPI"
  language_version: "Python 3.11+"
  build_tool: "pip/poetry"

testing:
  unit: "pytest"
  bdd: "behave"
  api: "requests"
```

### 4️⃣ .NET Core
```yaml
stack_id: "dotnet"
name: ".NET Core"
language: "C#"
version: "8.0"
status: "available"
description: "Microsoft .NET生態系統"

framework:
  core: ".NET 8.0"
  web: "ASP.NET Core"
  build_tool: "dotnet CLI"

testing:
  unit: "xUnit/NUnit"
  bdd: "SpecFlow"
```

## 🎯 當前專案配置

```yaml
current_project: "ExchangeRate"
selected_stack: "springboot"
locked: true
reason: "專案已使用Spring Boot架構建立"

enforced_constraints:
  - 所有代碼必須使用Java 17+
  - 使用Maven作為構建工具
  - 測試框架限制為JUnit 5 + Cucumber-JVM
  - API測試使用REST Assured
  - 資料庫ORM使用JPA/Hibernate
  - 不允許混用其他語言框架
```

## 🔒 技術棧限制規則

### 開發員 (Developer)
- **只能使用**當前技術棧的允許庫和工具
- **不可引入**其他語言或框架
- **必須遵循**技術棧的編碼規範

### 架構師 (Architect)
- **可以提議**技術棧變更
- **需要評估**新技術的相容性
- **必須考慮**團隊技能和維護成本

### QA測試員 (QA Tester)
- **使用指定**的測試框架和工具
- **Gherkin規格**必須與技術棧的BDD工具相容
- **測試工具**限制在技術棧範圍內

### 代碼審查員 (Code Reviewer)
- **檢查技術棧**合規性
- **拒絕不符合**技術棧規範的程式碼
- **確保一致性**與最佳實踐

## 📋 技術棧變更流程

1. **需求評估** - 分析變更必要性
2. **技術評估** - 評估新技術成熟度
3. **成本分析** - 學習成本與維護成本
4. **團隊投票** - 技術團隊討論投票
5. **遷移計劃** - 制定詳細遷移步驟
6. **風險評估** - 識別潛在風險
7. **決策確認** - 最終決策確認

## ⚙️ 工具整合

### IDE與編輯器配置
```yaml
recommended_ide:
  - "IntelliJ IDEA (Java)"
  - "Visual Studio Code (多語言)"
  - "Eclipse (Java)"

required_plugins:
  intellij:
    - "Lombok Plugin"
    - "Spring Boot Plugin"
    - "Cucumber for Java"
  vscode:
    - "Extension Pack for Java"
    - "Spring Boot Extension Pack"
    - "Cucumber (Gherkin)"
```

### CI/CD管道配置
```yaml
pipeline_constraints:
  build_tool: "Maven"
  test_command: "mvn test"
  integration_test: "mvn failsafe:integration-test"
  package_command: "mvn package"
  docker_base_image: "openjdk:17-jre-slim"
```