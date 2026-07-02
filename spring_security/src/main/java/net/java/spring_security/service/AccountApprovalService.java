package net.java.spring_security.service;

import net.java.spring_security.model.User;
import net.java.spring_security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AccountApprovalService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // ===== GET ALL PENDING ACCOUNTS =====
    public List<User> getPendingAccounts() {
        return userRepository.findByAccountStatus(User.AccountStatus.PENDING);
    }

    // ===== GET ALL ACCOUNTS BY STATUS =====
    public List<User> getAccountsByStatus(User.AccountStatus status) {
        return userRepository.findByAccountStatus(status);
    }

    // ===== APPROVE ACCOUNT =====
    public User approveAccount(Long userId, Integer approvedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEmailVerified()) {
            throw new RuntimeException(
                    "Cannot approve — email not verified yet");
        }

        user.setAccountStatus(User.AccountStatus.APPROVED);
        user.setEnabled(true);
        user.setApprovedBy(approvedBy);
        user.setApprovedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        // Send approval email
        emailService.sendAccountApprovedEmail(
                user.getEmail(), user.getFirstName());

        return saved;
    }

    // ===== REJECT ACCOUNT =====
    public User rejectAccount(Long userId, Integer rejectedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountStatus(User.AccountStatus.REJECTED);
        user.setEnabled(false);
        user.setApprovedBy(rejectedBy);
        user.setApprovedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        // Send rejection email
        emailService.sendAccountRejectedEmail(
                user.getEmail(), user.getFirstName());

        return saved;
    }

    // ===== FREEZE ACCOUNT =====
    public User freezeAccount(Long userId, Integer frozenBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountStatus(User.AccountStatus.FROZEN);
        user.setEnabled(false);

        User saved = userRepository.save(user);

        // Send freeze email
        emailService.sendAccountFrozenEmail(
                user.getEmail(), user.getFirstName());

        return saved;
    }

    // ===== UNFREEZE ACCOUNT =====
    public User unfreezeAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountStatus(User.AccountStatus.APPROVED);
        user.setEnabled(true);

        return userRepository.save(user);
    }
}