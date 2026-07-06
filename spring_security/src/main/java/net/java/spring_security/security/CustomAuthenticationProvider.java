package net.java.spring_security.security;

import net.java.spring_security.model.User;
import net.java.spring_security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Replaces DaoAuthenticationProvider so we have full control over
 * CERT-In style password policy checks during login:
 *   - account lockout after repeated failed attempts
 *   - password expiry (forced reset after N days)
 *   - failed-attempt counting / reset on success
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;
    private static final int PASSWORD_EXPIRY_DAYS = 90;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String email = authentication.getName();
        String rawPassword = authentication.getCredentials() != null
                ? authentication.getCredentials().toString() : "";

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // 1. Lockout check
        if (user.getLockedUntil() != null &&
                user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new LockedException(
                    "Account locked due to multiple failed attempts. Try again after "
                            + user.getLockedUntil());
        }

        // 2. Enabled check (email verified + manager approved)
        if (!user.isEnabled()) {
            throw new DisabledException("Account is not enabled. Please verify your email and wait for approval.");
        }

        // 3. Password check
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            registerFailedAttempt(user);
            throw new BadCredentialsException("Invalid email or password");
        }

        // 4. Password expiry check (only after password matched, so we don't
        //    leak expiry info to someone guessing passwords)
        if (user.getPasswordChangedAt() != null &&
                user.getPasswordChangedAt()
                        .plusDays(PASSWORD_EXPIRY_DAYS)
                        .isBefore(LocalDateTime.now())) {
            throw new CredentialsExpiredException(
                    "Your password has expired. Please reset it to continue.");
        }

        // 5. Success — reset failure tracking
        if (user.getFailedAttempts() != 0 || user.getLockedUntil() != null) {
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(user.getRole()));

        return new UsernamePasswordAuthenticationToken(email, rawPassword, authorities);
    }

    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            user.setFailedAttempts(0); // reset counter, lock timestamp now governs access
        }

        userRepository.save(user);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}