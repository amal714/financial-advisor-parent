package com.financial.advisor.portfolio.service;

import com.financial.advisor.portfolio.dto.PortfolioRequest;

public interface PortfolioService {
    void saveAndRequestAnalysis(String userId, PortfolioRequest request);
}