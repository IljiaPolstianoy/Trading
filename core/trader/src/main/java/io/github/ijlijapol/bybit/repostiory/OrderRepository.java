package io.github.ijlijapol.bybit.repostiory;

import io.github.ijlijapol.bybit.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
