package org.example.trade_executor.client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FauxnanceQuoteClient implements QuoteClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FauxnanceQuoteClient.class);

    private static final Pattern SYMBOL_PATTERN = Pattern.compile("\"symbol\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern PRICE_PATTERN = Pattern.compile("\"price\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");

    private final HttpClient httpClient;
    private final URI baseUri;
    private final int maxAttempts;
    private final long retryDelayMillis;
    private final Duration requestTimeout;
    private final String apiKey;

    @Autowired
    public FauxnanceQuoteClient(
            @Value("${trading.quote.base-url}") String baseUrl,
            @Value("${trading.quote.api-key:}") String apiKey,
            @Value("${trading.quote.max-attempts:3}") int maxAttempts,
            @Value("${trading.quote.retry-delay-millis:100}") long retryDelayMillis,
            @Value("${trading.quote.request-timeout-millis:1000}") long requestTimeoutMillis) {

        this(HttpClient.newBuilder()
                        .connectTimeout(Duration.ofMillis(requestTimeoutMillis))
                        .build(),
                URI.create(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/"),
                apiKey,
                maxAttempts,
                retryDelayMillis,
                Duration.ofMillis(requestTimeoutMillis));
    }

    FauxnanceQuoteClient(
            String baseUrl,
            int maxAttempts,
            long retryDelayMillis,
            long requestTimeoutMillis) {

        this(baseUrl, "", maxAttempts, retryDelayMillis, requestTimeoutMillis);
    }

    FauxnanceQuoteClient(
            HttpClient httpClient,
            URI baseUri,
            String apiKey,
            int maxAttempts,
            long retryDelayMillis,
            Duration requestTimeout) {

        this.httpClient = httpClient;
        this.baseUri = baseUri;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.maxAttempts = Math.max(1, maxAttempts);
        this.retryDelayMillis = Math.max(0L, retryDelayMillis);
        this.requestTimeout = requestTimeout;
    }

    @Override
    public Optional<LiveQuote> getQuote(String symbol) {
        URI quoteUri = resolveQuoteUri(symbol);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(quoteUri)
            .timeout(requestTimeout)
            .GET();

        if (!apiKey.isEmpty()) {
            requestBuilder.header("X-Api-Key", apiKey);
        }

        HttpRequest request = requestBuilder.build();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                HttpResponse<String> response = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return parseQuote(symbol, response.body());
                }

                if (response.statusCode() == 404 || response.statusCode() == 204) {
                    LOGGER.info("No quote available for symbol {} (HTTP {})", symbol, response.statusCode());
                    return Optional.empty();
                }

                if (isRetryable(response.statusCode()) && attempt < maxAttempts) {
                    LOGGER.warn(
                            "Retryable quote lookup failure for symbol {} at {} (HTTP {}, attempt {} of {})",
                            symbol,
                            quoteUri,
                            response.statusCode(),
                            attempt,
                            maxAttempts);
                    backoff(attempt);
                    continue;
                }

                LOGGER.error(
                    "Quote lookup failed for symbol {} at {} with HTTP {}",
                    symbol,
                    quoteUri,
                    response.statusCode());

                throw new QuoteUnavailableException(
                        symbol,
                        "Quote lookup failed with HTTP status " + response.statusCode());
            } catch (IOException ex) {
                if (attempt < maxAttempts) {
                    LOGGER.warn(
                            "I/O error during quote lookup for symbol {} at {} (attempt {} of {})",
                            symbol,
                            quoteUri,
                            attempt,
                            maxAttempts,
                            ex);
                    backoff(attempt);
                    continue;
                }

                LOGGER.error(
                    "Quote lookup failed after retries for symbol {} at {}",
                    symbol,
                    quoteUri,
                    ex);

                throw new QuoteUnavailableException(
                        symbol,
                        "Quote lookup failed after retries",
                        ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new QuoteUnavailableException(
                        symbol,
                        "Quote lookup interrupted",
                        ex);
            }
        }

        throw new QuoteUnavailableException(symbol, "Quote lookup failed after retries");
    }

    private Optional<LiveQuote> parseQuote(String symbol, String body) throws IOException {
        Matcher priceMatcher = PRICE_PATTERN.matcher(body == null ? "" : body);
        if (!priceMatcher.find()) {
            return Optional.empty();
        }

        String parsedSymbol = symbol;
        Matcher symbolMatcher = SYMBOL_PATTERN.matcher(body);
        if (symbolMatcher.find() && !symbolMatcher.group(1).isBlank()) {
            parsedSymbol = symbolMatcher.group(1);
        }

        return Optional.of(new LiveQuote(
                parsedSymbol,
                new BigDecimal(priceMatcher.group(1))));
    }

    private URI resolveQuoteUri(String symbol) {
        String encodedSymbol = URLEncoder.encode(symbol, StandardCharsets.UTF_8);
        return baseUri.resolve("quotes/" + encodedSymbol);
    }

    private boolean isRetryable(int statusCode) {
        return statusCode == 408
                || statusCode == 425
                || statusCode == 429
                || statusCode == 500
                || statusCode == 502
                || statusCode == 503
                || statusCode == 504;
    }

    private void backoff(int attempt) {
        if (retryDelayMillis <= 0) {
            return;
        }

        long delay = retryDelayMillis * Math.max(1, attempt);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new QuoteUnavailableException("unknown", "Quote retry interrupted", ex);
        }
    }

}



