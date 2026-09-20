package finadvisor.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PanValidator.class)
public @interface ValidPan {
    String message() default "PAN number must match the format AAAAA9999A";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
