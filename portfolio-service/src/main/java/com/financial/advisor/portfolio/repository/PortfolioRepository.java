package com.financial.advisor.portfolio.repository;

import com.financial.advisor.portfolio.domain.Portfolio;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface PortfolioRepository extends MongoRepository<Portfolio, String> {
    Optional<Portfolio> findByUserId(String userId);
}