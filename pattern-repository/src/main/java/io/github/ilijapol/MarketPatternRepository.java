package io.github.ilijapol;

import io.github.ilijapol.entity.MarketPattern;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketPatternRepository extends JpaRepository<MarketPattern, Integer> {
}
