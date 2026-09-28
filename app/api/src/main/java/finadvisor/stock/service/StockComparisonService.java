package finadvisor.stock.service;

import finadvisor.stock.dto.StockComparisonResponse;

import java.util.List;

public interface StockComparisonService {

    StockComparisonResponse compare(List<String> symbols);
}
