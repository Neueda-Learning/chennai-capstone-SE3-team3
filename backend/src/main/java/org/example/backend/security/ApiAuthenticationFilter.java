package org.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.exceptions.UnauthorisedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authenticates all /api/v1/* requests by verifying JWT Bearer token.
 * 
 * Check order: signature → expiry → algorithm (per story requirements).
 * Dev tokens (dev-account-N) are supported for testing.
 * All token failures return AUTH-401 (Unauthorised).
 */
@Component
public class ApiAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiAuthenticationFilter.class);
    private static final String DEV_TOKEN_PREFIX = "dev-account-";

    private final JwtVerifier jwtVerifier;

    public ApiAuthenticationFilter(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/") && !path.equals("/api/v1");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        
        // Check header presence and scheme
        if (header == null || !header.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header");
            throw new UnauthorisedException();
        }

        String token = header.substring(7).trim();
        if (token.isEmpty()) {
            logger.warn("Empty Bearer token");
            throw new UnauthorisedException();
        }

        Long accountId = extractAccountId(token);
        if (accountId == null || accountId < 1) {
            logger.warn("Failed to extract valid account ID from token");
            throw new UnauthorisedException();
        }

        request.setAttribute(AuthContext.ACCOUNT_ID_ATTRIBUTE, accountId);
        filterChain.doFilter(request, response);
    }

    /**
     * Extract account ID from token.
     * Tries dev token format first, then JWT verification.
     */
    private Long extractAccountId(String token) {
        // Support dev tokens for testing
        if (token.startsWith(DEV_TOKEN_PREFIX)) {
            return parseDevToken(token);
        }

        // Verify JWT token (signature → expiry → algorithm)
        try {
            return jwtVerifier.verifyAndExtractAccountId(token);
        } catch (JwtVerifier.JwtVerificationException e) {
            // All JWT failures logged by verifier; return null to trigger AUTH-401
            return null;
        }
    }

    private Long parseDevToken(String token) {
        try {
            String idStr = token.substring(DEV_TOKEN_PREFIX.length());
            return Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            logger.warn("Invalid dev token format: {}", token);
            return null;
        }
    }
}
