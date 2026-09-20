package com.financial.advisor.portfolio.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioRequest(List<String> tickers, BigDecimal totalValue) {}