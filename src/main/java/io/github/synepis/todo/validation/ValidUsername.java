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
@Constraint(validatedBy = {ValidUsername.Validator.class})
public @interface ValidUsername {
    String message() default "must be between 3 and 50 characters long and contain only [a-z0-9_] characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ValidUsername, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value != null && Constants.USERNAME_PATTTERN.matcher(value).matches();
        }
    }
}
