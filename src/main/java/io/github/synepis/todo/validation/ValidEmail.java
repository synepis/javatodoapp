package io.github.synepis.todo.validation;


import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = {ValidEmail.Validator.class})
public @interface ValidEmail {
    String message() default "must be valid email";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ValidEmail, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value != null && Constants.VALID_EMAIL_PATTERN.matcher(value).matches();
        }
    }
}
