//package com.example.gateway.util;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.web.reactive.server.WebTestClient;
//
///**
// * Gateway 路由测试工具
// */
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureWebTestClient
//public class GatewayTest {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    /**
//     * 测试 Gateway 健康检查
//     */
//    @Test
//    public void testHealthCheck() {
//        webTestClient.get()
//                .uri("/actuator/health")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.status").isEqualTo("UP");
//    }
//
//    /**
//     * 测试 Gateway 路由信息
//     */
//    @Test
//    public void testGatewayRoutes() {
//        webTestClient.get()
//                .uri("/actuator/gateway/routes")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$").isArray();
//    }
//
//    /**
//     * 测试用户服务路由
//     */
//    @Test
//    public void testUserServiceRoute() {
//        webTestClient.get()
//                .uri("/api/user/health")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.status").isEqualTo("UP");
//    }
//
//    /**
//     * 测试 TTS 服务路由
//     */
//    @Test
//    public void testTTSServiceRoute() {
//        webTestClient.get()
//                .uri("/api/tts/health")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.status").isEqualTo("UP");
//    }
//
//    /**
//     * 测试跨域配置
//     */
//    @Test
//    public void testCors() {
//        webTestClient.options()
//                .uri("/api/user/health")
//                .header("Origin", "http://localhost:3000")
//                .header("Access-Control-Request-Method", "GET")
//                .exchange()
//                .expectStatus().isOk();
//    }
//
//    /**
//     * 测试路径重写
//     */
//    @Test
//    public void testPathRewrite() {
//        webTestClient.get()
//                .uri("/api/user/test")
//                .exchange()
//                .expectStatus().isOk();
//    }
//}
