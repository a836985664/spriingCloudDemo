# Gateway Service

Spring Cloud Gateway API 网关服务

## 功能特性

- ✅ 服务路由转发
- ✅ 负载均衡 (LoadBalancer)
- ✅ 服务注册发现 (Nacos)
- ✅ 统一跨域配置
- ✅ 路径重写
- ✅ 健康检查 (Actuator)

## 路由配置

| 服务名 | 路由规则 | 示例 |
|--------|---------|------|
| user-service | `/api/user/**` → `lb://user-service` | `http://gateway:8080/api/user/login` |
| tts-service | `/api/tts/**` → `lb://tts-service` | `http://gateway:8080/api/tts/synthesize` |
| eureka-server | `/eureka/**` → `http://localhost:8761` | `http://gateway:8080/eureka/` |

## 启动步骤

1. 启动 Nacos (localhost:8848)
2. 启动其他服务 (user-service, tts-service, eureka-server)
3. 启动 Gateway 服务

```bash
cd gateway-service
mvn spring-boot:run
```

## 访问地址

- Gateway: `http://localhost:8080`
- 用户服务: `http://localhost:8080/api/user/**`
- TTS 服务: `http://localhost:8080/api/tts/**`
- 健康检查: `http://localhost:8080/actuator/health`
- Gateway 路由信息: `http://localhost:8080/actuator/gateway/routes`

## 配置说明

- `spring.cloud.gateway.discovery.locator.enabled`: 开启服务发现自动路由
- `spring.cloud.gateway.globalcors`: 全局跨域配置
- `StripPrefix=2`: 去掉 `/api` 前缀，将请求转发到服务内部路径

## idea配置启动nacos

1.添加运行/调试配置,添加新配置shell script
2.执行 选择脚本文本
3.脚本文本输入：./startup.cmd
4.工作目录：D:\ideaProject\stellar-infra\nacos\bin
5.保存