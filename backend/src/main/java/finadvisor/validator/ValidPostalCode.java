package finadvisor.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PostalCodeValidator.class)
public @interface ValidPostalCode {
    String message() default "Postal code must be a valid 6-digit Indian PIN code";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
