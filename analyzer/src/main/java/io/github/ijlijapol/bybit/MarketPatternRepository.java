package io.github.ijlijapol.bybit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketPatternRepository extends JpaRepository<MarketPattern, Integer> {
}
