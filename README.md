# ShangLuHua

鏈嶈鎵瑰彂杩涢攢瀛樹笌鍟嗗崟鏈嶅姟鍘熷瀷銆傚綋鍓嶉鏋堕€傚悎鐩存帴鐢?IntelliJ IDEA 鎵撳紑銆?
## 鎶€鏈爤

- Java 17
- Spring Boot 3.3
- Spring Web
- Spring Data JPA
- Spring Data Redis
- H2 鏈湴鏁版嵁搴擄紝鍚庣画鍙垏 PostgreSQL/MySQL
- Nginx 鍙嶅悜浠ｇ悊閰嶇疆绀轰緥

## 蹇€熷惎鍔?
1. 鍦?IDEA 涓墦寮€ `E:\IDEAworkplace\ShangLuHua`銆?2. 绛?Maven 瀵煎叆渚濊禆銆?3. 鍙€夛細杩愯 `scripts\start-redis.ps1` 鍚姩浣犵殑 Redis銆?4. 杩愯 `ShangLuHuaApplication`銆?5. 鎵撳紑 `http://localhost:8080/api/health`銆?
## 甯哥敤鎺ュ彛

- `GET /api/health`
- `POST /api/products`
- `GET /api/products`
- `POST /api/customers`
- `GET /api/customers`
- `POST /api/inventory/adjust`
- `GET /api/inventory/{skuId}`
- `POST /api/sales-orders`
- `POST /api/sales-orders/{id}/confirm`

## H2 鎺у埗鍙?
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./.h2/shangluhua`
- User: `sa`
- Password: 绌?'@

Write-ProjectFile 'src\main\resources\application.yml' @'
server:
  port: 8080

spring:
  application:
    name: shangluhua
  datasource:
    url: jdbc:h2:file:./.h2/shangluhua;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false
    properties:
      hibernate:
        format_sql: true
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2s

logging:
  level:
    com.shangluhua: INFO
