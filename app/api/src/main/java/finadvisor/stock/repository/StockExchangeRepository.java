package finadvisor.stock.repository;

import finadvisor.stock.entity.StockExchange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockExchangeRepository extends JpaRepository<StockExchange, UUID> {

    Optional<StockExchange> findByCode(String code);
}
