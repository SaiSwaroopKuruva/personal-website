package finadvisor.mutualfund.provider;

import java.time.LocalDate;

public record ProviderManagerData(
        String name,
        String designation,
        Integer experienceYears,
        LocalDate joiningDate,
        String bio,
        boolean active
) {
}
