package net.java.spring_security.banking.controller;

import net.java.spring_security.banking.model.KycDocument;
import net.java.spring_security.banking.repository.CustomerRepository;
import net.java.spring_security.banking.model.Customer;
import net.java.spring_security.banking.service.KycService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/kyc")
public class KycApiController {

    @Autowired
    private KycService kycService;

    @Autowired
    private CustomerRepository customerRepository;

    // ===== UPLOAD KYC (Customer) =====
    @PostMapping("/upload")
    public ResponseEntity<?> uploadKyc(
            @RequestParam("aadhaarNumber") String aadhaarNumber,
            @RequestParam("panNumber") String panNumber,
            @RequestParam("aadhaarFile") MultipartFile aadhaarFile,
            @RequestParam("panFile") MultipartFile panFile,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            List<Customer> customers = customerRepository.findByEmail(email);

            if (customers.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(error("Customer not found"));
            }

            Integer customerId = customers.get(0).getCustomerId();

            KycDocument kyc = kycService.uploadKyc(
                    customerId, aadhaarNumber, panNumber,
                    aadhaarFile, panFile);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "KYC documents uploaded successfully");
            response.put("kycId", kyc.getKycId());
            response.put("status", kyc.getStatus());
            response.put("submittedAt", kyc.getSubmittedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== GET MY KYC STATUS (Customer) =====
    @GetMapping("/status")
    public ResponseEntity<?> getMyKycStatus(Authentication authentication) {
        try {
            String email = authentication.getName();
            List<Customer> customers = customerRepository.findByEmail(email);

            if (customers.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(error("Customer not found"));
            }

            Integer customerId = customers.get(0).getCustomerId();
            Optional<KycDocument> kyc =
                    kycService.getKycByCustomerId(customerId);

            if (kyc.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("status", "NOT_SUBMITTED");
                response.put("message", "No KYC documents submitted yet");
                return ResponseEntity.ok(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("kycId", kyc.get().getKycId());
            response.put("status", kyc.get().getStatus());
            response.put("submittedAt", kyc.get().getSubmittedAt());
            response.put("reviewedAt", kyc.get().getReviewedAt());
            response.put("rejectionReason", kyc.get().getRejectionReason());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== GET ALL PENDING KYC (Manager/Executive) =====
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingKyc(Authentication authentication) {
        try {
            boolean isAuthorized = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                            a.getAuthority().equals("ROLE_MANAGER") ||
                            a.getAuthority().equals("ROLE_EXECUTIVE"));

            if (!isAuthorized) {
                return ResponseEntity.status(403)
                        .body(error("Access denied"));
            }

            List<KycDocument> pending = kycService.getPendingKyc();
            return ResponseEntity.ok(pending);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== GET ALL KYC (Manager/Executive) =====
    @GetMapping("/all")
    public ResponseEntity<?> getAllKyc(Authentication authentication) {
        try {
            boolean isAuthorized = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                            a.getAuthority().equals("ROLE_MANAGER") ||
                            a.getAuthority().equals("ROLE_EXECUTIVE"));

            if (!isAuthorized) {
                return ResponseEntity.status(403)
                        .body(error("Access denied"));
            }

            List<KycDocument> all = kycService.getAllKyc();
            return ResponseEntity.ok(all);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== VERIFY KYC (Manager/Executive) =====
    @PutMapping("/verify/{kycId}")
    public ResponseEntity<?> verifyKyc(@PathVariable Integer kycId,
                                       Authentication authentication) {
        try {
            boolean isAuthorized = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                            a.getAuthority().equals("ROLE_MANAGER") ||
                            a.getAuthority().equals("ROLE_EXECUTIVE"));

            if (!isAuthorized) {
                return ResponseEntity.status(403)
                        .body(error("Access denied"));
            }

            KycDocument kyc = kycService.verifyKyc(kycId, 1);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "KYC verified successfully");
            response.put("kycId", kyc.getKycId());
            response.put("status", kyc.getStatus());
            response.put("reviewedAt", kyc.getReviewedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== REJECT KYC (Manager/Executive) =====
    @PutMapping("/reject/{kycId}")
    public ResponseEntity<?> rejectKyc(@PathVariable Integer kycId,
                                       @RequestBody Map<String, String> request,
                                       Authentication authentication) {
        try {
            boolean isAuthorized = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                            a.getAuthority().equals("ROLE_MANAGER") ||
                            a.getAuthority().equals("ROLE_EXECUTIVE"));

            if (!isAuthorized) {
                return ResponseEntity.status(403)
                        .body(error("Access denied"));
            }

            String reason = request.get("reason");
            if (reason == null || reason.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(error("Rejection reason is required"));
            }

            KycDocument kyc = kycService.rejectKyc(kycId, reason, 1);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "KYC rejected");
            response.put("kycId", kyc.getKycId());
            response.put("status", kyc.getStatus());
            response.put("reason", kyc.getRejectionReason());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== VIEW/DOWNLOAD DOCUMENT (Manager/Executive) =====
    @GetMapping("/document/{kycId}/{type}")
    public ResponseEntity<?> viewDocument(@PathVariable Integer kycId,
                                          @PathVariable String type,
                                          Authentication authentication) {
        try {
            boolean isAuthorized = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                            a.getAuthority().equals("ROLE_MANAGER") ||
                            a.getAuthority().equals("ROLE_EXECUTIVE"));

            if (!isAuthorized) {
                return ResponseEntity.status(403)
                        .body(error("Access denied"));
            }

            KycDocument kyc = kycService.getAllKyc().stream()
                    .filter(k -> k.getKycId().equals(kycId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("KYC not found"));

            String filePath = type.equals("aadhaar") ?
                    kyc.getAadhaarUrl() : kyc.getPanUrl();

            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                return ResponseEntity.status(404)
                        .body(error("Document not found"));
            }

            String contentType = filePath.endsWith(".pdf") ?
                    "application/pdf" : "image/jpeg";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

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