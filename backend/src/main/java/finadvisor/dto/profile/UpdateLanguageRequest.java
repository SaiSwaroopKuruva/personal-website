package finadvisor.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateLanguageRequest(
        @NotBlank(message = "Preferred language is required")
        @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "Preferred language must be a valid language code, e.g. en or en-IN")
        String preferredLanguage
) {
}
