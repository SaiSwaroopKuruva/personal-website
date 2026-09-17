package finadvisor.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PostalCodeValidator implements ConstraintValidator<ValidPostalCode, String> {

    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile("^[1-9][0-9]{5}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return POSTAL_CODE_PATTERN.matcher(value).matches();
    }
}
