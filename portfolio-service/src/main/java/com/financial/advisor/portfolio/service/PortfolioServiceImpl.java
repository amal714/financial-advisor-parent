package com.financial.advisor.portfolio.service;

import com.financial.advisor.common.events.PortfolioAnalysisEvent;
import com.financial.advisor.portfolio.domain.Portfolio;
import com.financial.advisor.portfolio.dto.PortfolioRequest;
import com.financial.advisor.portfolio.messaging.KafkaEventPublisher;
import com.financial.advisor.portfolio.repository.PortfolioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository repository;
    private final KafkaEventPublisher eventPublisher;

    public PortfolioServiceImpl(PortfolioRepository repository, KafkaEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void saveAndRequestAnalysis(String userId, PortfolioRequest request) {
        // 1. Save or update portfolio state in MongoDB
        Portfolio portfolio = repository.findByUserId(userId)
                .map(existing -> {
                    // In a real app, you might merge tickers. Here we overwrite for simplicity.
                    return new Portfolio(userId, request.tickers(), request.totalValue());
                })
                .orElseGet(() -> new Portfolio(userId, request.tickers(), request.totalValue()));

        repository.save(portfolio);

        // 2. Create immutable event payload
        PortfolioAnalysisEvent event = new PortfolioAnalysisEvent(
                UUID.randomUUID(),
                userId,
                request.tickers(),
                request.totalValue(),
                LocalDateTime.now()
        );

        // 3. Publish to Kafka for async AI processing
        eventPublisher.publishAnalysisRequestedEvent(event);
    }
}