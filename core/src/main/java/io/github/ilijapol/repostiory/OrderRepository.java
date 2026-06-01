package io.github.ilijapol.repostiory;

import io.github.ilijapol.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
