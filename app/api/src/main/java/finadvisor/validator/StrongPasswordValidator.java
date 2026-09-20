package finadvisor.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;
import java.util.regex.Pattern;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password123!", "password1234", "qwerty123456", "letmein12345",
            "welcome12345", "admin1234567", "123456789012", "iloveyou1234");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value.length() < 12) {
            return false;
        }
        if (!UPPERCASE.matcher(value).find() || !LOWERCASE.matcher(value).find()
                || !DIGIT.matcher(value).find() || !SPECIAL.matcher(value).find()) {
            return false;
        }
        return !COMMON_PASSWORDS.contains(value.toLowerCase());
    }
}
