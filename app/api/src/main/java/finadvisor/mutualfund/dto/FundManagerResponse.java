package finadvisor.mutualfund.dto;

import java.time.LocalDate;

public record FundManagerResponse(
        String name,
        String designation,
        Integer experienceYears,
        LocalDate joiningDate,
        String bio,
        boolean active
) {
}
