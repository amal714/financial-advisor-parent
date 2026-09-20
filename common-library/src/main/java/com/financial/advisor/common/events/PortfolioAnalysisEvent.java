package com.financial.advisor.common.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PortfolioAnalysisEvent(
        UUID eventId,
        String userId,
        List<String> tickers,
        BigDecimal totalValue,
        LocalDateTime timestamp
) {}