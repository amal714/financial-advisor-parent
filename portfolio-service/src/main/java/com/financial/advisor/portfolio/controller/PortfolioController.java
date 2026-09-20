package com.financial.advisor.portfolio.controller;

import com.financial.advisor.portfolio.dto.PortfolioRequest;
import com.financial.advisor.portfolio.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<Void> analyzePortfolio(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody PortfolioRequest request) {

        portfolioService.saveAndRequestAnalysis(userId, request);

        // Return 202 Accepted indicating the request is being processed asynchronously
        return ResponseEntity.accepted().build();
    }
}