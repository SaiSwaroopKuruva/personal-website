package finadvisor.portfolio.dto;

import finadvisor.portfolio.entity.AssetType;
import finadvisor.portfolio.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull AssetType assetType,
        @Size(max = 50) String stockSymbol,
        @Size(max = 20) String stockExchange,
        @Size(max = 50) String mutualFundSchemeCode,
        @NotNull TransactionType transactionType,
        @NotNull LocalDate transactionDate,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal quantity,
        BigDecimal pricePerUnit,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal grossAmount,
        @DecimalMin(value = "0", inclusive = true) BigDecimal fees,
        @DecimalMin(value = "0", inclusive = true) BigDecimal taxes,
        @Size(max = 1000) String notes,
        @Size(max = 100) String externalReference
) {
}
