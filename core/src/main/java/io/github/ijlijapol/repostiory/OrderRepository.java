package io.github.ijlijapol.repostiory;

import io.github.ijlijapol.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
