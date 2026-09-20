package com.financial.advisor.gateway.filter;

import com.financial.advisor.common.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filterFactory;
    private GatewayFilter filter;
    private GatewayFilterChain filterChain;
    private JwtUtils jwtUtils;

    private final String SECRET = "my-super-secret-key-which-must-be-at-least-32-bytes-long";

    @BeforeEach
    void setUp() {
        filterFactory = new JwtAuthenticationFilter(SECRET, 3600000);
        filter = filterFactory.apply(new JwtAuthenticationFilter.Config());
        filterChain = mock(GatewayFilterChain.class);
        jwtUtils = new JwtUtils(SECRET, 3600000);

        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Test
    void shouldBlockRequestWithoutAuthorizationHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/portfolio/analyze").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, filterChain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void shouldBlockRequestWithInvalidTokenFormat() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/portfolio/analyze")
                .header(HttpHeaders.AUTHORIZATION, "Basic some-credentials")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, filterChain);

        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void shouldAllowRequestWithValidTokenAndAppendHeader() {
        String validToken = jwtUtils.generateToken("user-123", "ROLE_USER");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/portfolio/analyze")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, filterChain);

        StepVerifier.create(result).verifyComplete();

        // Verify that the filter chain was called and the X-User-Id header was injected
        verify(filterChain, times(1)).filter(argThat(mutatedExchange -> {
            String injectedHeader = mutatedExchange.getRequest().getHeaders().getFirst("X-User-Id");
            return "user-123".equals(injectedHeader);
        }));
    }
}