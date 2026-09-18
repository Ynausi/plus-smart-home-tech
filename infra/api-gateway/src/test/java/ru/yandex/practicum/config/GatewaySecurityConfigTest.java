package ru.yandex.practicum.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebTestClient
class GatewaySecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void productsGet_shouldBePublic() {
        webTestClient
                .get()
                .uri("/api/products")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void ordersPost_withoutCredentials_shouldReturn401() {
        webTestClient
                .post()
                .uri("/api/orders")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void ordersPost_withIvan_shouldPassSecurity() {
        webTestClient
                .post()
                .uri("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, basic("ivan", "ivan"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void productsModification_withIvan_shouldReturn403() {
        webTestClient
                .post()
                .uri("/api/products")
                .header(HttpHeaders.AUTHORIZATION, basic("ivan", "ivan"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void productsModification_withAnna_shouldPassSecurity() {
        webTestClient
                .post()
                .uri("/api/products")
                .header(HttpHeaders.AUTHORIZATION, basic("anna", "anna"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void allOrders_withIvan_shouldReturn403() {
        webTestClient
                .get()
                .uri("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, basic("ivan", "ivan"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void allOrders_withAnna_shouldPassSecurity() {
        webTestClient
                .get()
                .uri("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, basic("anna", "anna"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void unknownRoute_withAnna_shouldReturn403() {
        webTestClient
                .get()
                .uri("/unknown-route")
                .header(HttpHeaders.AUTHORIZATION, basic("anna", "anna"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void options_shouldPassSecurity() {
        webTestClient
                .method(HttpMethod.OPTIONS)
                .uri("/api/orders")
                .exchange()
                .expectStatus().isOk();
    }

    private static String basic(String username, String password) {
        String credentials = username + ":" + password;

        return "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    @TestConfiguration
    static class TestRoutes {

        @Bean
        RouterFunction<ServerResponse> testBackendRoutes() {
            return route(GET("/api/products"),
                    request -> ServerResponse.ok().build())

                    .andRoute(POST("/api/orders"),
                            request -> ServerResponse.ok().build())

                    .andRoute(POST("/api/products"),
                            request -> ServerResponse.ok().build())

                    .andRoute(GET("/api/orders"),
                            request -> ServerResponse.ok().build())

                    .andRoute(OPTIONS("/api/orders"),
                            request -> ServerResponse.ok().build());
        }
    }
}