package org.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.exceptions.UnauthorisedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ApiAuthenticationFilter extends OncePerRequestFilter {

    private static final Pattern ACCOUNT_ID_CLAIM = Pattern.compile("\\\"accountId\\\"\\s*:\\s*(?:\\\"(\\\\d+)\\\"|(\\\\d+))");

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
        if (header == null || !header.startsWith("Bearer ")) {
            throw new UnauthorisedException();
        }

        String token = header.substring(7).trim();
        Long accountId = parseAccountId(token);
        if (accountId == null || accountId < 1) {
            throw new UnauthorisedException();
        }

        request.setAttribute(AuthContext.ACCOUNT_ID_ATTRIBUTE, accountId);
        filterChain.doFilter(request, response);
    }

    private Long parseAccountId(String token) {
        if (token.startsWith("dev-account-")) {
            String id = token.substring("dev-account-".length());
            return parseLong(id);
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            String payload = new String(decoded, StandardCharsets.UTF_8);
            Matcher matcher = ACCOUNT_ID_CLAIM.matcher(payload);
            if (!matcher.find()) {
                return null;
            }
            String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            return parseLong(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static Long parseLong(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
