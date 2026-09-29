package finadvisor.portfolio.dto;

import finadvisor.portfolio.entity.AssetType;
import finadvisor.portfolio.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID portfolioId,
        AssetType assetType,
        String stockSymbol,
        String stockExchange,
        String mutualFundSchemeCode,
        TransactionType transactionType,
        LocalDate transactionDate,
        BigDecimal quantity,
        BigDecimal pricePerUnit,
        BigDecimal grossAmount,
        BigDecimal fees,
        BigDecimal taxes,
        BigDecimal netAmount,
        String notes,
        String externalReference,
        Instant createdAt,
        Instant updatedAt
) {
}
