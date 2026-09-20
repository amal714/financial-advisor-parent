package com.financial.advisor.common.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResearchCompletedEvent(
        UUID eventId,
        String userId,
        String aiAdvice,
        String s3ReportUrl,
        LocalDateTime timestamp
) {}