# 微信扫码登录功能说明

## 功能概述

这是一个模拟微信扫码登录的功能，实现了完整的扫码登录流程：
1. 电脑端显示二维码
2. 手机端扫码后显示确认登录页面
3. 手机端点击确认登录
4. 电脑端自动跳转到欢迎页面

## 文件说明

### 后端文件

- `WechatAuthService.java` - 微信登录服务，处理二维码生成、状态查询、登录确认等
- `WechatAuthController.java` - 微信登录控制器，提供API接口
- `LoginState.java` - 登录状态模型
- `QrcodeResponse.java` - 二维码响应DTO
- `LoginStatusResponse.java` - 登录状态响应DTO
- `MockScanRequest.java` - 模拟扫码请求DTO
- `LoginResponse.java` - 登录响应DTO
- `AuthUrlResponse.java` - 授权URL响应DTO

### 前端文件

- `wechat-login-test.html` - 电脑端扫码登录页面
- `mobile-confirm.html` - 手机端确认登录页面
- `welcome.html` - 欢迎页面（登录成功后跳转）

## API 接口

### 1. 获取二维码

**接口：** `GET /api/auth/wechat/qrcode`

**响应：**
```json
{
  "qrcodeUrl": "http://localhost:8082/mobile-confirm.html?state=xxx",
  "qrcodeImage": "data:image/png;base64,...",
  "state": "xxx"
}
```

### 2. 查询登录状态

**接口：** `GET /api/auth/wechat/status?state=xxx`

**响应：**
```json
{
  "status": "pending|success|expired",
  "token": "jwt_token"
}
```

### 3. 模拟扫码（测试用）

**接口：** `POST /api/auth/wechat/mock-scan`

**请求：**
```json
{
  "state": "xxx"
}
```

**响应：**
```
jwt_token
```

### 4. 获取微信授权URL

**接口：** `GET /api/auth/wechat/auth-url`

**响应：**
```json
{
  "authUrl": "https://mock-wechat.com/connect/oauth2/authorize?..."
}
```

### 5. 微信回调

**接口：** `GET /api/auth/wechat/callback?code=xxx&state=xxx`

**响应：**
```json
{
  "token": "jwt_token",
  "userInfo": {
    "userId": 1,
    "username": "微信用户_xxx",
    "nickname": "微信用户",
    "avatar": "https://mock-avatar.com/default.png"
  }
}
```

## 使用流程

### 完整流程

1. **电脑端访问登录页面**
   - 访问 `http://localhost:8082/wechat-login-test.html`
   - 点击"获取二维码"按钮
   - 页面显示二维码和state

2. **手机端扫码**
   - 用手机扫描电脑端显示的二维码
   - 手机浏览器打开 `mobile-confirm.html` 页面
   - 页面显示用户信息和确认登录按钮

3. **手机端确认登录**
   - 点击"确认登录"按钮
   - 调用 `/api/auth/wechat/mock-scan` 接口
   - 后端生成JWT Token并更新登录状态

4. **电脑端自动跳转**
   - 电脑端轮询检测到登录状态为 `success`
   - 自动跳转到 `welcome.html` 欢迎页面
   - 显示用户信息和Token

### 测试流程（不使用手机）

1. 访问 `http://localhost:8082/wechat-login-test.html`
2. 点击"获取二维码"按钮
3. 点击"模拟扫码"按钮（模拟手机扫码并确认登录）
4. 等待2秒后自动跳转到欢迎页面

## 技术实现

### 后端实现

1. **二维码生成**
   - 使用 `ZXing` 库生成二维码图片
   - 二维码内容为手机端确认页面的URL（包含state参数）
   - 二维码图片转换为Base64格式返回

2. **登录状态管理**
   - 使用 `ConcurrentHashMap` 存储登录状态（开发阶段）
   - 生产环境建议使用Redis
   - 状态包括：pending（待扫码）、success（成功）、expired（过期）
   - 二维码有效期：120秒

3. **JWT Token生成**
   - 使用 `JwtService` 生成JWT Token
   - Token包含用户信息（userId、username、nickname等）
   - Token保存在登录状态中，供PC端轮询获取

4. **用户管理**
   - 根据 `openid` 查询或创建用户
   - 模拟场景使用 `mock_openid_xxx` 作为openid
   - 新用户自动创建并保存到数据库

### 前端实现

1. **电脑端登录页面**
   - 调用 `/api/auth/wechat/qrcode` 获取二维码
   - 每2秒轮询 `/api/auth/wechat/status` 查询登录状态
   - 检测到登录成功后自动跳转到欢迎页面
   - Token保存到 `localStorage`

2. **手机端确认页面**
   - 从URL获取 `state` 参数
   - 显示用户信息和确认登录按钮
   - 点击确认登录调用 `/api/auth/wechat/mock-scan` 接口
   - 登录成功后显示成功提示

3. **欢迎页面**
   - 从 `localStorage` 获取Token
   - 解析Token显示用户信息
   - 提供进入首页、个人中心、退出登录等功能

## 配置说明

### application.yml 配置

```yaml
wechat:
  appid: mock_appid
  secret: mock_secret
  redirect-uri: http://localhost:8082/api/auth/wechat/callback
```

### 依赖说明

- `spring-boot-starter-web` - Web框架
- `spring-boot-starter-data-jpa` - JPA数据访问
- `com.google.zxing:core` - 二维码生成
- `io.jsonwebtoken:jjwt-api` - JWT Token生成

## 注意事项

1. **开发环境**
   - 当前使用内存Map存储登录状态，重启服务后状态丢失
   - 生产环境建议使用Redis存储登录状态

2. **二维码有效期**
   - 二维码有效期为120秒
   - 过期后需要重新获取二维码

3. **Token存储**
   - Token保存在 `localStorage` 中
   - 生产环境建议使用更安全的存储方式（如HttpOnly Cookie）

4. **安全性**
   - 当前为模拟实现，未对接真实的微信API
   - 生产环境需要对接微信开放平台API
   - 需要添加CSRF防护、HTTPS等安全措施

## 扩展功能

### 待实现功能

1. **真实微信API对接**
   - 对接微信开放平台OAuth2.0接口
   - 实现真实的扫码登录流程

2. **Redis存储**
   - 使用Redis存储登录状态
   - 支持分布式部署

3. **WebSocket推送**
   - 使用WebSocket替代轮询
   - 实时推送登录状态

4. **多端登录**
   - 支持多设备同时登录
   - 支持踢出其他设备

5. **登录日志**
   - 记录登录日志
   - 支持查看登录历史

## 测试

### 启动服务

```bash
cd D:\ideaProject\spriingCloudDemo\user-service
mvn spring-boot:run
```

### 访问页面

- 电脑端登录页面：http://localhost:8082/wechat-login-test.html
- 手机端确认页面：http://localhost:8082/mobile-confirm.html?state=xxx
- 欢迎页面：http://localhost:8082/welcome.html

### 测试步骤

1. 访问电脑端登录页面
2. 点击"获取二维码"按钮
3. 用手机扫描二维码（或点击"模拟扫码"按钮）
4. 在手机端点击"确认登录"按钮
5. 电脑端自动跳转到欢迎页面

## 问题排查

### 二维码无法显示

- 检查后端服务是否启动
- 检查端口是否正确（默认8082）
- 检查浏览器控制台是否有错误

### 登录状态一直为pending

- 检查state参数是否正确
- 检查手机端是否成功调用确认登录接口
- 检查后端日志是否有错误

### 无法跳转到欢迎页面

- 检查Token是否正确保存到localStorage
- 检查welcome.html页面是否存在
- 检查浏览器控制台是否有错误

## 联系方式

如有问题，请联系开发团队。
