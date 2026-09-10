package org.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.exceptions.UnauthorisedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ApiAuthenticationFilter.
 * Verifies Bearer token extraction and dev token support.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApiAuthenticationFilterTest {

    @Mock
    private JwtVerifier mockVerifier;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private FilterChain mockFilterChain;

    private ApiAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new ApiAuthenticationFilter(mockVerifier);
    }

    @Test
    void testShouldFilterOnlyApiV1Paths() {
        when(mockRequest.getRequestURI()).thenReturn("/api/v1/orders");
        boolean shouldFilter = !filter.shouldNotFilter(mockRequest);
        
        org.junit.jupiter.api.Assertions.assertTrue(shouldFilter);

        when(mockRequest.getRequestURI()).thenReturn("/health");
        boolean shouldNotFilter = filter.shouldNotFilter(mockRequest);
        
        org.junit.jupiter.api.Assertions.assertTrue(shouldNotFilter);
    }

    @Test
    void testMissingAuthorizationHeaderThrowsUnauthorizedException() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/api/v1/orders");
        when(mockRequest.getHeader("Authorization")).thenReturn(null);

        assertThrows(UnauthorisedException.class, () -> {
            try {
                filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);
            } catch (jakarta.servlet.ServletException | java.io.IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testInvalidBearerSchemeThrowsUnauthorizedException() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/api/v1/orders");
        when(mockRequest.getHeader("Authorization")).thenReturn("Basic token");

        assertThrows(UnauthorisedException.class, () -> {
            try {
                filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);
            } catch (jakarta.servlet.ServletException | java.io.IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testEmptyBearerTokenThrowsUnauthorizedException() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/api/v1/orders");
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer ");

        assertThrows(UnauthorisedException.class, () -> {
            try {
                filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);
            } catch (jakarta.servlet.ServletException | java.io.IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testDevTokenExtractsAccountId() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/api/v1/orders");
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer dev-account-42");

        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        verify(mockRequest).setAttribute(AuthContext.ACCOUNT_ID_ATTRIBUTE, 42L);
        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    void testInvalidDevTokenThrowsUnauthorizedException() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/api/v1/orders");
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer dev-account-not-a-number");

        assertThrows(UnauthorisedException.class, () -> {
            try {
                filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);
            } catch (jakarta.servlet.ServletException | java.io.IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testValidJwtTokenExtractsAccountId() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/api/v1/orders");
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(mockVerifier.verifyAndExtractAccountId("valid.jwt.token")).thenReturn(123L);

        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        verify(mockRequest).setAttribute(AuthContext.ACCOUNT_ID_ATTRIBUTE, 123L);
        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    void testJwtVerificationFailureThrowsUnauthorizedException() throws Exception {
        when(mockRequest.getRequestURI()).thenReturn("/api/v1/orders");
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer expired.jwt.token");
        when(mockVerifier.verifyAndExtractAccountId("expired.jwt.token"))
            .thenThrow(new JwtVerifier.JwtVerificationException("Token expired"));

        assertThrows(UnauthorisedException.class, () -> {
            try {
                filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);
            } catch (jakarta.servlet.ServletException | java.io.IOException e) {
                throw new RuntimeException(e);
            }
        });
        
        verify(mockFilterChain, never()).doFilter(mockRequest, mockResponse);
    }
}
