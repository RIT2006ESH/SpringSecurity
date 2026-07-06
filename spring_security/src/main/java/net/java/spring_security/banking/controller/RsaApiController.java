package net.java.spring_security.banking.controller;

import net.java.spring_security.banking.model.Customer;
import net.java.spring_security.banking.model.EncryptionHistory;
import net.java.spring_security.banking.repository.CustomerRepository;
import net.java.spring_security.banking.repository.EncryptionHistoryRepository;
import net.java.spring_security.service.RsaEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rsa")
public class RsaApiController {

    @Autowired
    private RsaEncryptionService rsaEncryptionService;

    @Autowired
    private EncryptionHistoryRepository historyRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // ===== GET PUBLIC KEY =====
    @GetMapping("/public-key")
    public ResponseEntity<?> getPublicKey() {
        Map<String, String> response = new HashMap<>();
        response.put("publicKey", rsaEncryptionService.getPublicKey());
        return ResponseEntity.ok(response);
    }

    // ===== ENCRYPT AADHAAR =====
    @PostMapping("/encrypt/aadhaar")
    public ResponseEntity<?> encryptAadhaar(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String aadhaar = request.get("aadhaar");

            if (!aadhaar.matches("\\d{12}")) {
                return ResponseEntity.badRequest()
                        .body(error("Aadhaar must be 12 digits"));
            }

            String encrypted = rsaEncryptionService.encrypt(aadhaar);

            saveHistory(authentication.getName(),
                    EncryptionHistory.OperationType.ENCRYPT,
                    EncryptionHistory.DataType.AADHAAR,
                    authentication.getName());

            Map<String, String> response = new HashMap<>();
            response.put("encrypted", encrypted);
            response.put("message", "Aadhaar encrypted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== ENCRYPT PAN =====
    @PostMapping("/encrypt/pan")
    public ResponseEntity<?> encryptPan(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String pan = request.get("pan");

            if (!pan.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
                return ResponseEntity.badRequest()
                        .body(error("Invalid PAN format (e.g. ABCDE1234F)"));
            }

            String encrypted = rsaEncryptionService.encrypt(pan);

            saveHistory(authentication.getName(),
                    EncryptionHistory.OperationType.ENCRYPT,
                    EncryptionHistory.DataType.PAN,
                    authentication.getName());

            Map<String, String> response = new HashMap<>();
            response.put("encrypted", encrypted);
            response.put("message", "PAN encrypted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== DECRYPT =====
    @PostMapping("/decrypt")
    public ResponseEntity<?> decrypt(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            boolean isAuthorized = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER") ||
                            a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAuthorized) {
                return ResponseEntity.status(403)
                        .body(error("Access denied — Manager only"));
            }

            String encrypted = request.get("encrypted");
            String dataType = request.get("dataType");
            String decrypted = rsaEncryptionService.decrypt(encrypted);

            saveHistory(authentication.getName(),
                    EncryptionHistory.OperationType.DECRYPT,
                    EncryptionHistory.DataType.valueOf(dataType),
                    authentication.getName());

            Map<String, String> response = new HashMap<>();
            response.put("decrypted", decrypted);
            response.put("message", "Decryption successful");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== GET ENCRYPTION HISTORY (last 3) =====
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Authentication authentication) {
        try {
            List<Customer> customers = customerRepository
                    .findByEmail(authentication.getName());

            if (customers.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(error("Customer not found"));
            }

            List<EncryptionHistory> history = historyRepository
                    .findLast3ByCustomerId(
                            customers.get(0).getCustomerId(),
                            PageRequest.of(0, 3));

            return ResponseEntity.ok(history);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== HELPER =====
    private void saveHistory(String email,
                             EncryptionHistory.OperationType opType,
                             EncryptionHistory.DataType dataType,
                             String performedBy) {
        List<Customer> customers = customerRepository.findByEmail(email);
        if (!customers.isEmpty()) {
            EncryptionHistory history = new EncryptionHistory();
            history.setCustomerId(customers.get(0).getCustomerId());
            history.setOperationType(opType);
            history.setDataType(dataType);
            history.setPerformedAt(LocalDateTime.now());
            history.setPerformedBy(performedBy);
            historyRepository.save(history);
        }
    }

    private Map<String, String> error(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}