package com.financial.advisor.portfolio.service;

import com.financial.advisor.common.events.PortfolioAnalysisEvent;
import com.financial.advisor.portfolio.domain.Portfolio;
import com.financial.advisor.portfolio.dto.PortfolioRequest;
import com.financial.advisor.portfolio.messaging.KafkaEventPublisher;
import com.financial.advisor.portfolio.repository.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceImplTest {

    @Mock
    private PortfolioRepository repository;

    @Mock
    private KafkaEventPublisher eventPublisher;

    @InjectMocks
    private PortfolioServiceImpl portfolioService;

    private final String USER_ID = "user-123";
    private PortfolioRequest request;

    @BeforeEach
    void setUp() {
        request = new PortfolioRequest(List.of("AAPL", "MSFT"), new BigDecimal("10000.00"));
    }

    @Test
    void shouldSaveNewPortfolioAndPublishEvent() {
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(repository.save(any(Portfolio.class))).thenAnswer(i -> i.getArguments()[0]);

        portfolioService.saveAndRequestAnalysis(USER_ID, request);

        // Verify MongoDB Save
        ArgumentCaptor<Portfolio> portfolioCaptor = ArgumentCaptor.forClass(Portfolio.class);
        verify(repository, times(1)).save(portfolioCaptor.capture());
        assertEquals(USER_ID, portfolioCaptor.getValue().getUserId());
        assertEquals(2, portfolioCaptor.getValue().getTickers().size());

        // Verify Kafka Publish
        ArgumentCaptor<PortfolioAnalysisEvent> eventCaptor = ArgumentCaptor.forClass(PortfolioAnalysisEvent.class);
        verify(eventPublisher, times(1)).publishAnalysisRequestedEvent(eventCaptor.capture());

        PortfolioAnalysisEvent publishedEvent = eventCaptor.getValue();
        assertNotNull(publishedEvent.eventId());
        assertEquals(USER_ID, publishedEvent.userId());
        assertEquals(new BigDecimal("10000.00"), publishedEvent.totalValue());
    }
}