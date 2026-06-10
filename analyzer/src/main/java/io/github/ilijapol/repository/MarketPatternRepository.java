package io.github.ilijapol.repository;

import io.github.ilijapol.entity.MarketPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketPatternRepository extends JpaRepository<MarketPattern, Integer> {
}
