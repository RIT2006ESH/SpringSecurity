package net.java.spring_security.service;

import net.java.spring_security.dto.RegistrationRequest;
import net.java.spring_security.model.EmailVerificationToken;
import net.java.spring_security.model.PasswordHistory;
import net.java.spring_security.model.User;
import net.java.spring_security.repository.EmailVerificationTokenRepository;
import net.java.spring_security.repository.PasswordHistoryRepository;
import net.java.spring_security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final int PASSWORD_HISTORY_LIMIT = 5;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private PasswordHistoryRepository passwordHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public User registerUser(RegistrationRequest request, String role) {

        if (!request.isPasswordMatching()) {
            throw new RuntimeException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            User existingUser = userRepository
                    .findByEmail(request.getEmail()).get();
            tokenRepository.findByUser(existingUser)
                    .ifPresent(tokenRepository::delete);
            userRepository.delete(existingUser);
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setEmailVerified(false);
        user.setEnabled(false);
        user.setAccountStatus(User.AccountStatus.PENDING);
        user.setPasswordChangedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Seed password history so future reuse-checks have something to compare against
        passwordHistoryRepository.save(
                new PasswordHistory(savedUser, savedUser.getPassword()));

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken =
                new EmailVerificationToken(token, savedUser);
        tokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                token);

        return savedUser;
    }

    public User registerUser(RegistrationRequest request) {
        return registerUser(request, "ROLE_CUSTOMER");
    }

    public boolean verifyEmail(String token) {
        Optional<EmailVerificationToken> optionalToken =
                tokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) return false;

        EmailVerificationToken verificationToken = optionalToken.get();

        if (verificationToken.isExpired()) return false;

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setEnabled(false); // stays disabled until manager approves
        user.setAccountStatus(User.AccountStatus.PENDING);
        userRepository.save(user);

        tokenRepository.delete(verificationToken);

        return true;
    }

    /**
     * Changes a user's password after checking it isn't one of their
     * last PASSWORD_HISTORY_LIMIT passwords (CERT-In password history rule).
     * Throws RuntimeException if the new password was recently used.
     */
    public void changePassword(User user, String newRawPassword) {
        List<PasswordHistory> recentHistory =
                passwordHistoryRepository.findByUserOrderByCreatedAtDesc(
                        user, PageRequest.of(0, PASSWORD_HISTORY_LIMIT));

        boolean reused = recentHistory.stream()
                .anyMatch(h -> passwordEncoder.matches(newRawPassword, h.getPasswordHash()));

        if (reused) {
            throw new RuntimeException(
                    "You cannot reuse any of your last " + PASSWORD_HISTORY_LIMIT + " passwords");
        }

        String newHash = passwordEncoder.encode(newRawPassword);
        user.setPassword(newHash);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        passwordHistoryRepository.save(new PasswordHistory(user, newHash));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}