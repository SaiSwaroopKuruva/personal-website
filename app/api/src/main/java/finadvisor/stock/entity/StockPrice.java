package finadvisor.stock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** One persisted OHLC candle (Part 9/10) - either a single END_OF_DAY row per trading day or a coarse INTRADAY aggregate; never one row per tick. */
@Entity
@Table(name = "stock_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id", "priceTimestamp", "dataType"})
public class StockPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "price_timestamp", nullable = false)
    private Instant priceTimestamp;

    @Column(precision = 14, scale = 4)
    private BigDecimal open;

    @Column(precision = 14, scale = 4)
    private BigDecimal high;

    @Column(precision = 14, scale = 4)
    private BigDecimal low;

    @Column(precision = 14, scale = 4)
    private BigDecimal close;

    @Column(name = "last_traded_price", precision = 14, scale = 4)
    private BigDecimal lastTradedPrice;

    private Long volume;

    @Column(nullable = false, length = 50)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private PriceDataType dataType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
