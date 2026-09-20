package com.financial.advisor.portfolio.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "portfolios")
public class Portfolio {
    @Id
    private String id;
    private String userId;
    private List<String> tickers;
    private BigDecimal totalValue;
    private LocalDateTime lastUpdated;

    public Portfolio() {}

    public Portfolio(String userId, List<String> tickers, BigDecimal totalValue) {
        this.userId = userId;
        this.tickers = tickers;
        this.totalValue = totalValue;
        this.lastUpdated = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public List<String> getTickers() { return tickers; }
    public BigDecimal getTotalValue() { return totalValue; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
}