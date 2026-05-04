package io.github.ijlijapol.bybit.repository;

import io.github.ijlijapol.bybit.model.TestPattern;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatternTestRepository extends JpaRepository<TestPattern, Integer> {
}
