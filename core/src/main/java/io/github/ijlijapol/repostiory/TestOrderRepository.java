package io.github.ijlijapol.repostiory;

import io.github.ijlijapol.model.TestOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestOrderRepository extends JpaRepository<TestOrder, Integer> {
}
