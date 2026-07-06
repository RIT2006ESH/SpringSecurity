package net.java.spring_security.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotCommonPasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotCommonPassword {
    String message() default "This password is too common — please choose a stronger one";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}