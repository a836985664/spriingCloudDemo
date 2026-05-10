# Gateway Service - 部署配置说明

## 📋 已创建文件清单

```
gateway-service/
├── pom.xml                          # Maven 依赖配置
├── src/main/java/com/example/gateway/
│   └── GatewayApplication.java      # 启动类
├── src/main/resources/
│   ├── application.yml              # 开发环境配置
│   ├── application-prod.yml         # 生产环境配置
│   ├── application-nacos.yml        # Nacos 动态配置示例
│   ├── bootstrap.yml                # Nacos 客户端配置
│   └── gateway-routes-example.yml   # 高级路由示例
├── util/GatewayTest.java            # 测试工具类
├── Dockerfile                       # Docker 构建文件
├── start-gateway.bat                # Windows 启动脚本
├── README.md                        # 使用说明
└── SETUP.md                         # 本文档
```

## 🚀 快速启动

### 方式一：使用 Maven 本地启动

```bash
# 1. 启动 Nacos (确保 8848 端口可用)
cd D:\ideaProject\spriingCloudDemo
mvn -pl nacos-server -Dspring-boot.run.profiles=standalone spring-boot:run

# 2. 启动其他微服务
mvn -pl user-service spring-boot:run
mvn -pl tts-service spring-boot:run

# 3. 启动 Gateway
cd gateway-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 方式二：使用 Docker Compose 一键启动

```bash
cd D:\ideaProject\spriingCloudDemo
docker-compose up -d
```

访问地址：
- Gateway: `http://localhost:8080`
- Nacos: `http://localhost:8848/nacos`
- Eureka: `http://localhost:8761`
- User Service: `http://localhost:8081/api/user/`
- TTS Service: `http://localhost:8082/api/tts/`

## 📦 Maven 依赖说明

### 核心依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| spring-cloud-starter-gateway | 2024.0.1 | Spring Cloud Gateway 核心 |
| spring-cloud-starter-loadbalancer | 2024.0.1 | 负载均衡器 |
| spring-cloud-starter-alibaba-nacos-discovery | 2023.0.3.3 | 服务注册发现 |
| spring-cloud-starter-alibaba-nacos-config | 2023.0.3.3 | Nacos 配置中心 |
| spring-boot-starter-actuator | 3.4.5 | 健康检查和监控 |

## 🌐 路由配置

### 基础路由

```yaml
spring:
  cloud:
    gateway:
      routes:
        # 用户服务
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=2

        # TTS 服务
        - id: tts-service
          uri: lb://tts-service
          predicates:
            - Path=/api/tts/**
          filters:
            - StripPrefix=2
```

### 高级路由特性

#### 1. 路由优先级
```yaml
order: 1000  # 数字越小优先级越高
```

#### 2. 路由超时
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 3000
        response-timeout: 5s
```

#### 3. 限流
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: rate-limiter
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

#### 4. 重试
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: retry-route
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - name: Retry
              args:
                retries: 3
                statuses: 5XX
                methods: GET,POST
```

#### 5. 跨域配置
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600
```

## 🔧 Nacos 配置

### Nacos 配置中心

在 Nacos 控制台添加配置文件：

**Data ID**: `gateway-service.yml`
**Group**: `DEFAULT_GROUP`
**配置格式**: YAML

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-route
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=2
```

### 启用动态路由

```yaml
# application.yml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: public
        group: DEFAULT_GROUP
        file-extension: yml
        refresh-enabled: true
```

## 🧪 测试

### 使用 WebTestClient

```java
@SpringBootTest
@AutoConfigureWebTestClient
public class GatewayTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testHealthCheck() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void testUserServiceRoute() {
        webTestClient.get()
                .uri("/api/user/health")
                .exchange()
                .expectStatus().isOk();
    }
}
```

### 使用 curl 测试

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 路由信息
curl http://localhost:8080/actuator/gateway/routes

# 用户服务路由
curl http://localhost:8080/api/user/health

# TTS 服务路由
curl http://localhost:8080/api/tts/health
```

## 🐳 Docker 部署

### 构建镜像

```bash
cd gateway-service
docker build -t gateway-service:1.0.0 .
```

### 运行容器

```bash
docker run -d \
  --name gateway-service \
  -p 8080:8080 \
  -e SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR=nacos:8848 \
  -e SPRING_CLOUD_NACOS_CONFIG_SERVER_ADDR=nacos:8848 \
  gateway-service:1.0.0
```

### 使用 Docker Compose

```bash
docker-compose up -d gateway-service
```

## 📊 监控指标

### Actuator 端点

| 端点 | 说明 |
|------|------|
| `/actuator/health` | 健康检查 |
| `/actuator/info` | 应用信息 |
| `/actuator/metrics` | 监控指标 |
| `/actuator/gateway/routes` | 路由信息 |
| `/actuator/gateway/instances` | 服务实例 |

### 关键指标

- `http.server.requests`: HTTP 请求统计
- `gateway.requests`: Gateway 特定指标
- `system.cpu.usage`: CPU 使用率
- `jvm.memory.used`: JVM 内存使用

## 🔐 安全配置

### 启用 JWT 认证

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: jwt-auth-route
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
            - Header=Authorization, Bearer
          filters:
            - StripPrefix=2
            - name: JWTFilter
```

### 请求头过滤

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: header-filter
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - name: AddRequestHeader
              args:
                name: X-Request-Source
                value: gateway-service
```

## 📝 环境变量

### 开发环境

```bash
export SPRING_PROFILES_ACTIVE=dev
export NACOS_SERVER=localhost:8848
export NACOS_NAMESPACE=public
export NACOS_GROUP=DEFAULT_GROUP
```

### 生产环境

```bash
export SPRING_PROFILES_ACTIVE=prod
export NACOS_SERVER=prod-nacos:8848
export NACOS_NAMESPACE=production
export NACOS_GROUP=PROD_GROUP
export REGION=cn-north
```

## 🐛 故障排查

### 常见问题

1. **服务无法注册到 Nacos**
   - 检查 Nacos 是否启动
   - 检查网络连接
   - 查看日志中的 Nacos 连接错误

2. **路由不生效**
   - 检查服务名是否正确
   - 检查路由配置格式
   - 查看 `/actuator/gateway/routes` 路由信息

3. **跨域问题**
   - 检查 CORS 配置
   - 确认 allowedOrigins 设置

4. **限流不生效**
   - 检查 Redis 是否可用
   - 检查限流参数配置

### 日志位置

- 开发环境: `logs/gateway-service.log`
- Docker 环境: `容器内 /app/logs/gateway-service.log`

## 📚 参考资源

- [Spring Cloud Gateway 官方文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [Spring Cloud Alibaba Nacos 文档](https://github.com/alibaba/spring-cloud-alibaba/wiki/Nacos)
- [Spring Boot Actuator 文档](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

## 🎯 下一步优化建议

1. **集成 Sentinel**: 添加熔断降级机制
2. **集成 Spring Security**: 添加 JWT 认证
3. **集成 Resilience4j**: 添加断路器
4. **集成 Prometheus**: 添加监控告警
5. **集成 SkyWalking**: 添加分布式追踪
6. **添加日志收集**: 集成 ELK 或 Loki
7. **添加灰度发布**: 实现流量灰度
