package io.github.ilijapol.repostiory;

import io.github.ilijapol.model.TestOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestOrderRepository extends JpaRepository<TestOrder, Integer> {
}
