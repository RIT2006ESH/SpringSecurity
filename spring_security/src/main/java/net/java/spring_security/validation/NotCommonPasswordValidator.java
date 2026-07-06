package net.java.spring_security.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class NotCommonPasswordValidator
        implements ConstraintValidator<NotCommonPassword, String> {

    // A representative blocklist of the most common/breached passwords.
    // Extend this list (or load from a file) as needed.
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password", "password1", "password123", "password@123",
            "12345678", "123456789", "qwerty123", "qwertyuiop",
            "admin123", "admin@123", "welcome123", "welcome@123",
            "letmein123", "iloveyou1", "abc123456", "abcd1234",
            "test1234", "test@1234", "changeme1", "india123",
            "banking123", "secure123", "P@ssw0rd", "P@ssword1"
    );

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return true; // let @NotBlank handle nulls/blanks
        }
        return !COMMON_PASSWORDS.contains(password.toLowerCase());
    }
}