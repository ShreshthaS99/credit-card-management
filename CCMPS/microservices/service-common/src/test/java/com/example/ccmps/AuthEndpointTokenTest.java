package com.example.ccmps;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PlatformSecurityTest.TestApplication.class, properties = {
        "spring.application.name=auth-service",
        "app.jwt.secret=test-only-signing-key-with-at-least-thirty-two-bytes",
        "app.jwt.issuer=ccmps-auth",
        "app.jwt.audience=ccmps-api",
        "app.cors.origin=http://localhost:5173"
})
class AuthEndpointTokenTest {
    @Autowired private WebApplicationContext context;

    @Test
    void staleBearerDoesNotRejectPublicAuthenticationRequestsBeforeController() throws Exception {
        var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        for (String path : new String[]{"/api/auth/login", "/api/auth/register"}) {
            // These fixtures have no auth controller; 404 proves token validation did not reject the request.
            mvc.perform(post(path).servletPath(path)
                            .header("Authorization", "Bearer invalid-stale-token"))
                    .andExpect(status().isNotFound());
        }
    }
}
