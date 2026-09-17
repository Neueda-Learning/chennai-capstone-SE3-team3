package org.example.trade_executor.client;

import java.util.Optional;

public interface QuoteClient {

    Optional<LiveQuote> getQuote(String symbol);
}

