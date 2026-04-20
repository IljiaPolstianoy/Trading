package io.github.ijlijapol.bybit.repository;

import io.github.ijlijapol.bybit.model.TestOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestOrderRepository extends JpaRepository<TestOrder, Integer> {
}
