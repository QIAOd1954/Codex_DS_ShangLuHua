# 商陆花 (ShangLuHua)

服装批发进销存与商单服务原型。

## 技术栈

- Java 17
- Spring Boot 3.3.5
- Spring Web
- Spring Data JPA
- Spring Data Redis
- Spring Security + JWT
- MySQL / H2 数据库
- Nginx 反向代理配置示例

## 快速启动

### 前置要求

- Java 17 JDK
- MySQL（默认配置）或 H2（开发模式）
- Redis（可选，用于缓存）

### 使用 MySQL 启动

```bash
# 配置环境变量或修改 application-mysql.yml
$env:JAVA_HOME="E:\JAVAjdk17"; $env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run
```

### 使用 H2 启动

```bash
# 修改 application.yml 中 profiles.active 为 h2
$env:JAVA_HOME="E:\JAVAjdk17"; $env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run -Dspring.profiles.active=h2
```

### 启动后访问

| 页面 | 地址 | 说明 |
|------|------|------|
| 管理后台 | http://localhost:8080 | 登录后管理商品/客户/订单/入库 |
| 买家 H5 | http://localhost:8080/buyer | 批发选购下单 |
| 健康检查 | http://localhost:8080/api/health | API 状态 |

默认管理员账号：`admin` / `123456`

## API 接口

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 登录获取 JWT Token |
| GET | /api/auth/me | 获取当前用户信息 |

### 商品管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/products | 创建商品（含SKU） |
| GET | /api/products | 搜索商品列表 |
| GET | /api/products/{id} | 获取商品详情 |
| PUT | /api/products/{id} | 更新商品 |
| DELETE | /api/products/{id} | 删除商品 |

### 客户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/customers | 创建客户 |
| GET | /api/customers | 搜索客户列表 |
| GET | /api/customers/{id} | 获取客户详情 |
| PUT | /api/customers/{id} | 更新客户 |
| DELETE | /api/customers/{id} | 删除客户 |

### 库存管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/inventory/adjust | 调整库存 |
| GET | /api/inventory/{skuId} | 查询SKU库存 |

### 销售订单

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/sales-orders | 创建订单 |
| GET | /api/sales-orders | 搜索订单（支持关键词/状态/日期） |
| GET | /api/sales-orders/{id} | 订单详情 |
| POST | /api/sales-orders/{id}/confirm | 确认订单（扣库存+记欠款） |
| POST | /api/sales-orders/{id}/cancel | 取消订单（恢复库存+扣欠款） |

### 采购入库

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/purchase-orders | 创建入库单 |
| GET | /api/purchase-orders | 搜索入库单 |
| GET | /api/purchase-orders/{id} | 入库单详情 |
| POST | /api/purchase-orders/{id}/confirm | 确认入库（增加库存） |
| POST | /api/purchase-orders/{id}/cancel | 取消入库单 |

### 对外公开接口（无需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/share/products | 商品列表 |
| POST | /api/share/orders | 创建订单 |
| GET | /api/share/orders?phone=xxx | 按手机号查订单 |

### 文件上传

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/upload | 上传图片（multipart/form-data） |

## 项目结构

```
shangluhua/
├── src/
│   └── main/
│       ├── java/com/shangluhua/app/
│       │   ├── auth/          # JWT 认证
│       │   ├── common/        # 通用（异常处理、文件上传、种子数据）
│       │   ├── customer/      # 客户管理
│       │   ├── inventory/     # 库存管理
│       │   ├── product/       # 商品管理
│       │   ├── purchase/      # 采购入库
│       │   └── sales/         # 销售订单
│       └── resources/
│           ├── static/        # 前端静态文件（管理后台）
│           └── application.yml
├── web/
│   └── buyer-h5/              # 买家 H5 页面（Nginx 托管）
├── nginx/                     # Nginx 配置
└── docs/                      # 文档
```

## 数据库

默认使用 MySQL，首次启动自动建表。配置文件见 `application-mysql.yml`。

H2 控制台访问：http://localhost:8080/h2-console（需在 application-h2.yml 配置下启用）
