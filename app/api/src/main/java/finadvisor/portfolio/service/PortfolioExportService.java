package finadvisor.portfolio.service;

import java.util.UUID;

/** CSV exports for reporting (Part 17). Applies the same ownership authorization as the JSON APIs. */
public interface PortfolioExportService {

    String exportHoldingsCsv(String userEmail, UUID portfolioId);

    String exportTransactionsCsv(String userEmail, UUID portfolioId);

    String exportSummaryCsv(String userEmail, UUID portfolioId);
}
