package finadvisor.stock.repository;

import finadvisor.stock.entity.PriceDataType;
import finadvisor.stock.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface StockPriceRepository extends JpaRepository<StockPrice, UUID> {

    boolean existsByStock_IdAndPriceTimestampAndDataType(UUID stockId, Instant priceTimestamp, PriceDataType dataType);

    List<StockPrice> findByStock_IdAndDataTypeAndPriceTimestampBetweenOrderByPriceTimestampAsc(
            UUID stockId, PriceDataType dataType, Instant from, Instant to);
}
