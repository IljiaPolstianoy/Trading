package io.github.ijlijapol.bybit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface Repository extends JpaRepository<TestOrder, Integer> {
}
