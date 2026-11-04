package com.example.ccmps;

import com.example.ccmps.client.RemoteFailurePredicate;
import feign.FeignException;
import feign.Request;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CircuitBreakerTest {
    @Test
    void dependencyFailuresOpenCircuitWithoutReturningFakeSuccess() {
        CircuitBreaker breaker = CircuitBreaker.of("test", CircuitBreakerConfig.custom()
                .slidingWindowSize(5).minimumNumberOfCalls(5)
                .recordException(new RemoteFailurePredicate()).build());
        for (int i = 0; i < 5; i++) {
            assertThrows(IllegalStateException.class, () -> breaker.executeSupplier(() -> {
                throw new IllegalStateException("dependency unavailable");
            }));
        }
        assertEquals(CircuitBreaker.State.OPEN, breaker.getState());
    }

    @Test
    void businessRejectionsDoNotTripCircuit() {
        Request request = Request.create(Request.HttpMethod.GET, "http://example.test",
                Map.of(), new byte[0], StandardCharsets.UTF_8, null);
        FeignException rejected = new FeignException.NotFound("missing", request, new byte[0], Map.of());
        assertFalse(new RemoteFailurePredicate().test(rejected));
        assertTrue(new RemoteFailurePredicate().test(
                new FeignException.ServiceUnavailable("down", request, new byte[0], Map.of())));
    }
}
