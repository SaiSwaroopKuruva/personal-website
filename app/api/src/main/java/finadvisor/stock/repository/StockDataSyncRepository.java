package finadvisor.stock.repository;

import finadvisor.stock.entity.StockDataSync;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockDataSyncRepository extends JpaRepository<StockDataSync, UUID> {
}
