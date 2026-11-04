package com.example.ccmps;

import com.example.ccmps.client.AccountClient;
import com.example.ccmps.security.AccessChecks;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest(classes = PlatformSecurityTest.TestApplication.class, properties = {
        "spring.application.name=test-service",
        "app.jwt.secret=test-only-signing-key-with-at-least-thirty-two-bytes",
        "app.jwt.issuer=ccmps-auth",
        "app.jwt.audience=ccmps-api",
        "app.cors.origin=http://localhost:5173",
        "spring.config.import=classpath:platform-defaults.properties"
})
class PlatformSecurityTest {
    @Autowired private WebApplicationContext context;
    @Autowired private SecretKey key;
    @Autowired private CircuitBreakerRegistry breakers;
    private MockMvc mvc;
    private static final AtomicReference<String> relayed = new AtomicReference<>();
    private static final AtomicBoolean unavailable = new AtomicBoolean();
    private static final AtomicInteger dependencyCalls = new AtomicInteger();
    private static final HttpServer stub = createStub();

    private static HttpServer createStub() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/api/accounts/", exchange -> {
                dependencyCalls.incrementAndGet();
                relayed.set(exchange.getRequestHeaders().getFirst("Authorization"));
                byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(unavailable.get() ? 503 : 200, body.length);
                try (var output = exchange.getResponseBody()) {
                    output.write(body);
                }
            });
            server.start();
            return server;
        } catch (java.io.IOException ex) {
            throw new java.io.UncheckedIOException(ex);
        }
    }

    @DynamicPropertySource
    static void feignUrl(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.openfeign.client.config.accountClient.url",
                () -> "http://127.0.0.1:" + stub.getAddress().getPort());
    }

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        unavailable.set(false);
        dependencyCalls.set(0);
        breakers.getAllCircuitBreakers().forEach(
                io.github.resilience4j.circuitbreaker.CircuitBreaker::reset);
    }

    @org.junit.jupiter.api.AfterAll
    static void stopStub() {
        stub.stop(0);
    }

    @Test
    void missingTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/test")).andExpect(status().isUnauthorized());
    }

    @Test
    void downstreamUnauthorizedResponseDoesNotAddCorsHeaders() throws Exception {
        mvc.perform(get("/api/test").header("Origin", "http://localhost:5173"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void cardholderCannotAccessAdminOrInternalEndpoints() throws Exception {
        String token = token("CARDHOLDER", "ccmps-auth", "ccmps-api", Instant.now().plusSeconds(120));
        mvc.perform(get("/api/admin/test").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/internal/test").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAdminEndpoint() throws Exception {
        mvc.perform(get("/api/admin/test").header("Authorization", "Bearer " +
                token("ADMIN", "ccmps-auth", "ccmps-api", Instant.now().plusSeconds(120))))
                .andExpect(status().isOk());
    }

    @Test
    void wrongIssuerAndAudienceAndExpiredTokensAreRejected() throws Exception {
        for (String token : List.of(
                token("ADMIN", "wrong", "ccmps-api", Instant.now().plusSeconds(120)),
                token("ADMIN", "ccmps-auth", "wrong", Instant.now().plusSeconds(120)),
                token("ADMIN", "ccmps-auth", "ccmps-api", Instant.now().minusSeconds(120)))) {
            mvc.perform(get("/api/test").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void customerOwnershipIsChecked() throws Exception {
        mvc.perform(get("/api/test").header("Authorization", "Bearer " +
                token("CARDHOLDER", "ccmps-auth", "ccmps-api", Instant.now().plusSeconds(120))))
                .andExpect(status().isForbidden());
    }

    @Test
    void bearerTokenSurvivesActualFeignCircuitBreakerInvocation() throws Exception {
        String bearer = "Bearer " + token("ADMIN", "ccmps-auth", "ccmps-api",
                Instant.now().plusSeconds(120));
        mvc.perform(get("/api/relay").header("Authorization", bearer))
                .andExpect(status().isOk());
        org.junit.jupiter.api.Assertions.assertTrue(bearer.equals(relayed.get()),
                "The inbound bearer token must reach the Feign dependency.");
    }

    @Test
    void configuredFeignCircuitOpensAfterFiveFailuresAndRejectsWithoutCallingDependency()
            throws Exception {
        unavailable.set(true);
        String bearer = "Bearer " + token("ADMIN", "ccmps-auth", "ccmps-api",
                Instant.now().plusSeconds(120));
        for (int i = 0; i < 6; i++) {
            mvc.perform(get("/api/relay").header("Authorization", bearer))
                    .andExpect(status().isServiceUnavailable());
        }
        org.junit.jupiter.api.Assertions.assertEquals(5, dependencyCalls.get());
        org.junit.jupiter.api.Assertions.assertTrue(breakers.getAllCircuitBreakers().stream()
                .anyMatch(b -> b.getState()
                        == io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN));
    }

    private String token(String role, String issuer, String audience, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder().subject("test-user")
                .issuer(issuer).audience(List.of(audience))
                .issuedAt(Instant.now().minusSeconds(300)).expiresAt(expiresAt)
                .claim("role", role).claim("customerId", UUID.randomUUID().toString()).build();
        return new NimbusJwtEncoder(new ImmutableSecret<>(key))
                .encode(JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    @SpringBootApplication
    @EnableFeignClients(clients = AccountClient.class)
    @Import(TestController.class)
    static class TestApplication {
    }

    @RestController
    static class TestController {
        private final AccessChecks access;
        TestController(AccessChecks access) {
            this.access = access;
        }
        @GetMapping("/api/test")
        String owned() {
            access.customer(UUID.fromString("00000000-0000-0000-0000-000000000001"));
            return "ok";
        }
        @GetMapping("/api/admin/test")
        String admin() {
            return "ok";
        }
        @GetMapping("/internal/test")
        String internal() {
            return "ok";
        }
        @GetMapping("/api/relay")
        String relay() {
            access.account(UUID.randomUUID());
            return "ok";
        }
    }
}
