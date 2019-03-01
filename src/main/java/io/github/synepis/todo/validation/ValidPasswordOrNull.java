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
@Constraint(validatedBy = {ValidPasswordOrNull.Validator.class})
public @interface ValidPasswordOrNull {
    String message() default "must be between 8 and 50 characters or null";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ValidPasswordOrNull, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value == null || Constants.PASSWORD_PATTTERN.matcher(value).matches();
        }
    }
}
