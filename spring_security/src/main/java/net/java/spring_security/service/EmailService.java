package net.java.spring_security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ===== EMAIL VERIFICATION =====
    public void sendVerificationEmail(String toEmail,
                                      String firstName,
                                      String token) {
        String verificationLink =
                "http://localhost:8080/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Verify Your Email Address");
        message.setText(
                "Hi " + firstName + ",\n\n" +
                        "Thank you for registering. Please click the link below " +
                        "to verify your email:\n\n" +
                        verificationLink + "\n\n" +
                        "This link expires in 24 hours.\n\n" +
                        "If you did not register, please ignore this email.\n\n" +
                        "Regards,\nSecureBank Team"
        );
        mailSender.send(message);
    }

    // ===== ACCOUNT APPROVED =====
    public void sendAccountApprovedEmail(String toEmail, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Your SecureBank Account Has Been Approved");
        message.setText(
                "Hi " + firstName + ",\n\n" +
                        "Great news! Your SecureBank account has been reviewed " +
                        "and approved by our team.\n\n" +
                        "You can now login and access all banking services:\n" +
                        "http://localhost:8080/login\n\n" +
                        "If you have any questions, please contact our support team.\n\n" +
                        "Regards,\nSecureBank Team"
        );
        mailSender.send(message);
    }

    // ===== ACCOUNT REJECTED =====
    public void sendAccountRejectedEmail(String toEmail, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Your SecureBank Account Application Status");
        message.setText(
                "Hi " + firstName + ",\n\n" +
                        "We regret to inform you that your SecureBank account " +
                        "application has been rejected after review.\n\n" +
                        "This may be due to incomplete or incorrect information " +
                        "provided during registration.\n\n" +
                        "You may re-apply at:\n" +
                        "http://localhost:8080/register\n\n" +
                        "If you believe this is a mistake, please contact " +
                        "our support team.\n\n" +
                        "Regards,\nSecureBank Team"
        );
        mailSender.send(message);
    }

    // ===== ACCOUNT FROZEN =====
    public void sendAccountFrozenEmail(String toEmail, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Important: Your SecureBank Account Has Been Frozen");
        message.setText(
                "Hi " + firstName + ",\n\n" +
                        "Your SecureBank account has been temporarily frozen " +
                        "due to suspicious activity detected on your account.\n\n" +
                        "For security reasons, all transactions have been " +
                        "suspended until further review.\n\n" +
                        "Please contact our support team immediately to " +
                        "resolve this issue.\n\n" +
                        "Regards,\nSecureBank Team"
        );
        mailSender.send(message);
    }

    // ===== KYC VERIFIED =====
    public void sendKycVerifiedEmail(String toEmail, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("KYC Verified Successfully | SecureBank");
        message.setText(
                "Hi " + firstName + ",\n\n" +
                        "Your KYC documents have been successfully verified.\n\n" +
                        "You now have full access to all SecureBank services.\n\n" +
                        "Login here: http://localhost:8080/login\n\n" +
                        "Regards,\nSecureBank Team"
        );
        mailSender.send(message);
    }

    // ===== KYC REJECTED - RE-UPLOAD LINK =====
    public void sendKycRejectedEmail(String toEmail,
                                     String firstName,
                                     String reason,
                                     String reUploadToken) {
        String reUploadLink =
                "http://localhost:8080/kyc/reupload?token=" + reUploadToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Action Required: KYC Re-verification Needed");
        message.setText(
                "Hi " + firstName + ",\n\n" +
                        "Your KYC documents have been reviewed and require " +
                        "re-submission.\n\n" +
                        "Reason: " + reason + "\n\n" +
                        "Please re-upload your documents using the secure link below:\n" +
                        reUploadLink + "\n\n" +
                        "This link expires in 24 hours.\n\n" +
                        "Regards,\nSecureBank Team"
        );
        mailSender.send(message);
    }
}