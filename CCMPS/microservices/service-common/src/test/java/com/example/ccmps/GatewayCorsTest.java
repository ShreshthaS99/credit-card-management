package com.example.ccmps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PlatformSecurityTest.TestApplication.class, properties = {
        "spring.application.name=api-gateway",
        "app.jwt.secret=test-only-signing-key-with-at-least-thirty-two-bytes",
        "app.jwt.issuer=ccmps-auth",
        "app.jwt.audience=ccmps-api",
        "app.cors.origin=http://localhost:5173"
})
class GatewayCorsTest {
    @Autowired private WebApplicationContext context;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void loginPreflightAllowsConfiguredFrontend() throws Exception {
        mvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(
                        "Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void unauthorizedResponseContainsExactlyOneAllowedOrigin() throws Exception {
        mvc.perform(get("/api/admin/test").header("Origin", "http://localhost:5173"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().stringValues(
                        "Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void untrustedOriginIsRejected() throws Exception {
        mvc.perform(options("/api/auth/login")
                        .header("Origin", "http://untrusted.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void staleBearerDoesNotBlockPublicAuthenticationEndpoints() throws Exception {
        for (String path : new String[]{"/api/auth/login", "/api/auth/register"}) {
            mvc.perform(post(path).servletPath(path)
                            .header("Origin", "http://localhost:5173")
                            .header("Authorization", "Bearer invalid-stale-token"))
                    .andExpect(status().isNotFound())
                    .andExpect(header().stringValues(
                            "Access-Control-Allow-Origin", "http://localhost:5173"));
        }
    }

    @Test
    void staleBearerIsStillRejectedOnProtectedEndpoints() throws Exception {
        mvc.perform(get("/api/admin/test")
                        .header("Authorization", "Bearer invalid-stale-token"))
                .andExpect(status().isUnauthorized());
    }
}
