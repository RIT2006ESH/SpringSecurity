package net.java.spring_security.controller;

import net.java.spring_security.model.User;
import net.java.spring_security.service.AccountApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/approval")
public class AccountApprovalController {

    @Autowired
    private AccountApprovalService accountApprovalService;

    // ===== CHECK ROLE HELPER =====
    private boolean isManager(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER") ||
                        a.getAuthority().equals("ROLE_ADMIN"));
    }

    // ===== GET ALL PENDING ACCOUNTS =====
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingAccounts(Authentication authentication) {
        try {
            if (!isManager(authentication)) {
                return ResponseEntity.status(403)
                        .body(error("Access denied — Manager only"));
            }

            List<User> pending = accountApprovalService.getPendingAccounts();

            if (pending.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "No pending accounts found");
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(pending);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== GET ACCOUNTS BY STATUS =====
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getAccountsByStatus(
            @PathVariable String status,
            Authentication authentication) {
        try {
            if (!isManager(authentication)) {
                return ResponseEntity.status(403)
                        .body(error("Access denied — Manager only"));
            }

            User.AccountStatus accountStatus =
                    User.AccountStatus.valueOf(status.toUpperCase());
            List<User> accounts =
                    accountApprovalService.getAccountsByStatus(accountStatus);

            return ResponseEntity.ok(accounts);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(error("Invalid status. Use: PENDING, APPROVED, REJECTED, FROZEN"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== APPROVE ACCOUNT =====
    @PutMapping("/approve/{userId}")
    public ResponseEntity<?> approveAccount(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            if (!isManager(authentication)) {
                return ResponseEntity.status(403)
                        .body(error("Access denied — Manager only"));
            }

            User approved = accountApprovalService.approveAccount(userId, 1);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Account approved successfully");
            response.put("userId", approved.getId());
            response.put("email", approved.getEmail());
            response.put("status", approved.getAccountStatus());
            response.put("approvedAt", approved.getApprovedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== REJECT ACCOUNT =====
    @PutMapping("/reject/{userId}")
    public ResponseEntity<?> rejectAccount(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            if (!isManager(authentication)) {
                return ResponseEntity.status(403)
                        .body(error("Access denied — Manager only"));
            }

            User rejected = accountApprovalService.rejectAccount(userId, 1);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Account rejected");
            response.put("userId", rejected.getId());
            response.put("email", rejected.getEmail());
            response.put("status", rejected.getAccountStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== FREEZE ACCOUNT =====
    @PutMapping("/freeze/{userId}")
    public ResponseEntity<?> freezeAccount(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            if (!isManager(authentication)) {
                return ResponseEntity.status(403)
                        .body(error("Access denied — Manager only"));
            }

            User frozen = accountApprovalService.freezeAccount(userId, 1);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Account frozen successfully");
            response.put("userId", frozen.getId());
            response.put("email", frozen.getEmail());
            response.put("status", frozen.getAccountStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== UNFREEZE ACCOUNT =====
    @PutMapping("/unfreeze/{userId}")
    public ResponseEntity<?> unfreezeAccount(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            if (!isManager(authentication)) {
                return ResponseEntity.status(403)
                        .body(error("Access denied — Manager only"));
            }

            User unfrozen = accountApprovalService.unfreezeAccount(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Account unfrozen successfully");
            response.put("userId", unfrozen.getId());
            response.put("email", unfrozen.getEmail());
            response.put("status", unfrozen.getAccountStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== HELPER =====
    private Map<String, String> error(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}