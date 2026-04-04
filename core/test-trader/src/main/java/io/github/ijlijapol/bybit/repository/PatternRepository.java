package io.github.ijlijapol.bybit.repository;

import io.github.ijlijapol.bybit.model.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatternRepository extends JpaRepository<Pattern,Integer> {
}
