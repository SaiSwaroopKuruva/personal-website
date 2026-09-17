package finadvisor.dto.profile;

import finadvisor.entity.Gender;
import finadvisor.validator.ValidPan;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateProfileRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must be at most 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must be at most 100 characters")
        String lastName,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        Gender gender,

        @Size(max = 100, message = "Occupation must be at most 100 characters")
        String occupation,

        @DecimalMin(value = "0", message = "Annual income cannot be negative")
        BigDecimal annualIncome,

        @DecimalMin(value = "0", message = "Monthly expenses cannot be negative")
        BigDecimal monthlyExpenses,

        @ValidPan
        String panNumber,

        @Pattern(regexp = "^$|^[0-9]{4}$", message = "Aadhaar last four must be exactly 4 digits")
        String aadhaarLastFour
) {
}
