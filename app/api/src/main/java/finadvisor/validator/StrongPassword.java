package finadvisor.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {
    String message() default "Password must be at least 12 characters and include an uppercase letter, "
            + "a lowercase letter, a number, a special character, and must not be a commonly used password";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
