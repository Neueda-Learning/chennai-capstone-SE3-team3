package org.example.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * Verifies JWT tokens in order: signature → expiry → algorithm → claims.
 * All token failures are logged server-side for investigation, never returned to client.
 */
@Component
public class JwtVerifier {

    private static final Logger logger = LoggerFactory.getLogger(JwtVerifier.class);
    private static final String ACCOUNT_ID_CLAIM = "accountId";
    private static final String ALGORITHM_CLAIM = "alg";

    private final JwtConfiguration jwtConfig;

    public JwtVerifier(JwtConfiguration jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    /**
     * Verifies JWT token and extracts account ID.
     * Check order: signature → expiry → algorithm → claims.
     * 
     * @param token the JWT token string
     * @return the account ID from the token
     * @throws JwtVerificationException if any verification fails
     */
    public Long verifyAndExtractAccountId(String token) {
        try {
            return verifyTokenSignature(token);
        } catch (JwtVerificationException e) {
            // Already logged by specific handler
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error verifying token", e);
            throw new JwtVerificationException("Token verification failed");
        }
    }

    private Long verifyTokenSignature(String token) {
        SecretKey key = jwtConfig.getSigningKey();
        
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // Verify algorithm (implicitly checked above, but can add explicit check)
            String algorithm = claims.get(ALGORITHM_CLAIM, String.class);
            if (algorithm != null && !algorithm.startsWith("HS")) {
                logger.error("Unexpected algorithm in token: {}", algorithm);
                throw new JwtVerificationException("Invalid algorithm");
            }
            
            // Extract and validate account ID
            Long accountId = extractAccountId(claims);
            if (accountId == null || accountId < 1) {
                logger.error("Invalid accountId in token: {}", accountId);
                throw new JwtVerificationException("Invalid account ID in token");
            }
            
            return accountId;
            
        } catch (SignatureException e) {
            logger.warn("Invalid JWT signature: {}", e.getMessage());
            throw new JwtVerificationException("Invalid signature");
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token has expired: {}", e.getMessage());
            throw new JwtVerificationException("Token expired");
        } catch (UnsupportedJwtException e) {
            logger.warn("Unsupported JWT format: {}", e.getMessage());
            throw new JwtVerificationException("Unsupported token format");
        } catch (MalformedJwtException e) {
            logger.warn("Malformed JWT: {}", e.getMessage());
            throw new JwtVerificationException("Malformed token");
        } catch (JwtException e) {
            logger.warn("JWT verification failed: {}", e.getMessage());
            throw new JwtVerificationException("Token verification failed");
        }
    }

    private Long extractAccountId(Claims claims) {
        Object rawAccountId = claims.get(ACCOUNT_ID_CLAIM);
        if (rawAccountId == null) {
            return null;
        }
        
        if (rawAccountId instanceof Number num) {
            return num.longValue();
        } else if (rawAccountId instanceof String str) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException e) {
                logger.error("Invalid accountId format: {}", str);
                return null;
            }
        }
        
        return null;
    }

    /**
     * Custom exception for JWT verification failures.
     * All verification failures map to AUTH-401 (Unauthorised).
     */
    public static class JwtVerificationException extends RuntimeException {
        public JwtVerificationException(String message) {
            super(message);
        }
    }
}
