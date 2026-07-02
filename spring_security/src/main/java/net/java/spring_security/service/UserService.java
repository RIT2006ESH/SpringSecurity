package net.java.spring_security.service;

import net.java.spring_security.dto.RegistrationRequest;
import net.java.spring_security.model.EmailVerificationToken;
import net.java.spring_security.model.User;
import net.java.spring_security.repository.EmailVerificationTokenRepository;
import net.java.spring_security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

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
        user.setAccountStatus(User.AccountStatus.PENDING); // ← NEW

        User savedUser = userRepository.save(user);

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

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}