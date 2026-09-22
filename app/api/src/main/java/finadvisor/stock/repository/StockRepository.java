package finadvisor.stock.repository;

import finadvisor.stock.entity.Stock;
import finadvisor.stock.entity.StockStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {

    Optional<Stock> findByInstrumentKey(String instrumentKey);

    Optional<Stock> findBySymbolIgnoreCaseAndStatusNot(String symbol, StockStatus excludedStatus);

    List<Stock> findByStatusNot(StockStatus excludedStatus);

    List<Stock> findBySymbolInIgnoreCase(List<String> symbols);

    List<Stock> findByCompanyNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(String companyName, String symbol);
}
