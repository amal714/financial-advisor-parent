package com.financial.advisor.portfolio.messaging;

import com.financial.advisor.common.events.PortfolioAnalysisEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher {

    private final KafkaTemplate<String, PortfolioAnalysisEvent> kafkaTemplate;
    private final String topic;

    public KafkaEventPublisher(
            KafkaTemplate<String, PortfolioAnalysisEvent> kafkaTemplate,
            @Value("${kafka.topic.portfolio-analysis}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishAnalysisRequestedEvent(PortfolioAnalysisEvent event) {
        // We use the userId as the Kafka message key to ensure events for the same user go to the same partition
        kafkaTemplate.send(topic, event.userId(), event);
    }
}