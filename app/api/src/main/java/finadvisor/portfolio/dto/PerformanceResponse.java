package finadvisor.portfolio.dto;

import java.time.LocalDate;
import java.util.List;

public record PerformanceResponse(
        String range,
        List<PerformancePoint> points,
        boolean limitedHistory,
        LocalDate earliestAvailableDate,
        String limitationMessage
) {
}
