package net.java.spring_security.banking.service;

import net.java.spring_security.banking.model.KycDocument;
import net.java.spring_security.banking.repository.CustomerRepository;
import net.java.spring_security.banking.repository.KycDocumentRepository;
import net.java.spring_security.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class KycService {

    @Autowired
    private KycDocumentRepository kycDocumentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmailService emailService;

    @Value("${file.upload.dir}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // ===== UPLOAD KYC DOCUMENTS =====
    public KycDocument uploadKyc(Integer customerId,
                                 String aadhaarNumber,
                                 String panNumber,
                                 MultipartFile aadhaarFile,
                                 MultipartFile panFile) throws IOException {

        // Validate file sizes
        if (aadhaarFile.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("Aadhaar file exceeds 5MB limit");
        }
        if (panFile.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("PAN file exceeds 5MB limit");
        }

        // Validate file types
        validateFileType(aadhaarFile);
        validateFileType(panFile);

        // Validate Aadhaar number (12 digits)
        if (!aadhaarNumber.matches("\\d{12}")) {
            throw new RuntimeException(
                    "Aadhaar number must be exactly 12 digits");
        }

        // Validate PAN number (format: ABCDE1234F)
        if (!panNumber.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
            throw new RuntimeException(
                    "Invalid PAN format (e.g. ABCDE1234F)");
        }

        // Create upload directory if not exists
        Path uploadPath = Paths.get(uploadDir + "/" + customerId);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save Aadhaar file
        String aadhaarFileName = "aadhaar_" + UUID.randomUUID() + "_" +
                aadhaarFile.getOriginalFilename();
        Path aadhaarPath = uploadPath.resolve(aadhaarFileName);
        Files.copy(aadhaarFile.getInputStream(), aadhaarPath);

        // Save PAN file
        String panFileName = "pan_" + UUID.randomUUID() + "_" +
                panFile.getOriginalFilename();
        Path panPath = uploadPath.resolve(panFileName);
        Files.copy(panFile.getInputStream(), panPath);

        // Check if KYC already exists for this customer
        Optional<KycDocument> existing =
                kycDocumentRepository.findByCustomerId(customerId);

        KycDocument kyc = existing.orElse(new KycDocument());
        kyc.setCustomerId(customerId);
        kyc.setAadhaarNumber(aadhaarNumber);
        kyc.setPanNumber(panNumber);
        kyc.setAadhaarUrl(uploadPath + "/" + aadhaarFileName);
        kyc.setPanUrl(uploadPath + "/" + panFileName);
        kyc.setStatus(KycDocument.KycStatus.PENDING);
        kyc.setSubmittedAt(LocalDateTime.now());
        kyc.setRejectionReason(null);

        return kycDocumentRepository.save(kyc);
    }

    // ===== GET KYC BY CUSTOMER =====
    public Optional<KycDocument> getKycByCustomerId(Integer customerId) {
        return kycDocumentRepository.findByCustomerId(customerId);
    }

    // ===== GET ALL PENDING KYC =====
    public List<KycDocument> getPendingKyc() {
        return kycDocumentRepository
                .findByStatus(KycDocument.KycStatus.PENDING);
    }

    // ===== GET ALL KYC =====
    public List<KycDocument> getAllKyc() {
        return kycDocumentRepository.findAll();
    }

    // ===== VERIFY KYC =====
    public KycDocument verifyKyc(Integer kycId, Integer reviewedBy) {
        KycDocument kyc = kycDocumentRepository.findById(kycId)
                .orElseThrow(() -> new RuntimeException("KYC not found"));

        kyc.setStatus(KycDocument.KycStatus.VERIFIED);
        kyc.setReviewedAt(LocalDateTime.now());
        kyc.setReviewedBy(reviewedBy);
        kyc.setRejectionReason(null);

        KycDocument saved = kycDocumentRepository.save(kyc);

        // Send KYC verified email
        try {
            customerRepository.findById(kyc.getCustomerId())
                    .ifPresent(customer ->
                            emailService.sendKycVerifiedEmail(
                                    customer.getEmail(),
                                    customer.getFirstName()));
        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }

        return saved;
    }

    // ===== REJECT KYC =====
    public KycDocument rejectKyc(Integer kycId,
                                 String reason,
                                 Integer reviewedBy) {
        KycDocument kyc = kycDocumentRepository.findById(kycId)
                .orElseThrow(() -> new RuntimeException("KYC not found"));

        kyc.setStatus(KycDocument.KycStatus.REJECTED);
        kyc.setReviewedAt(LocalDateTime.now());
        kyc.setReviewedBy(reviewedBy);
        kyc.setRejectionReason(reason);

        KycDocument saved = kycDocumentRepository.save(kyc);

        // Send KYC rejection email with re-upload link
        try {
            String reUploadToken = UUID.randomUUID().toString();
            customerRepository.findById(kyc.getCustomerId())
                    .ifPresent(customer ->
                            emailService.sendKycRejectedEmail(
                                    customer.getEmail(),
                                    customer.getFirstName(),
                                    reason,
                                    reUploadToken));
        } catch (Exception e) {
            System.out.println("Email failed: " + e.getMessage());
        }

        return saved;
    }

    // ===== VALIDATE FILE TYPE =====
    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") &&
                        !contentType.equals("image/png") &&
                        !contentType.equals("application/pdf"))) {
            throw new RuntimeException(
                    "Only JPG, PNG and PDF files are allowed");
        }
    }
}
