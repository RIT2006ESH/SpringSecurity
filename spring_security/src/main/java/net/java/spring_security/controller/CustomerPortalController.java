package net.java.spring_security.controller;

import net.java.spring_security.banking.model.Account;
import net.java.spring_security.banking.model.Customer;
import net.java.spring_security.banking.model.KycDocument;
import net.java.spring_security.banking.model.Transaction;
import net.java.spring_security.banking.repository.CustomerRepository;
import net.java.spring_security.banking.service.AccountService;
import net.java.spring_security.banking.service.KycService;
import net.java.spring_security.banking.service.TransactionService;
import net.java.spring_security.model.User;
import net.java.spring_security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class CustomerPortalController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private KycService kycService;

    @GetMapping("/dashboard")
    public String customerDashboard(Authentication authentication,
                                    Model model) {
        String email = authentication.getName();

        // Step 1: Load user
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            model.addAttribute("errorMessage", "User not found");
            return "customer/dashboard";
        }
        User user = optionalUser.get();
        model.addAttribute("user", user);
        model.addAttribute("accountStatus", user.getAccountStatus());

        // Step 2: Load customer from banking DB
        List<Customer> customers = customerRepository.findByEmail(email);
        if (customers.isEmpty()) {
            model.addAttribute("accounts", new ArrayList<>());
            model.addAttribute("transactions", new ArrayList<>());
            return "customer/dashboard";
        }

        Customer customer = customers.get(0);
        model.addAttribute("customer", customer);

        // Step 3: Load accounts
        List<Account> accounts = accountService
                .getAccountsByCustomerId(customer.getCustomerId());
        model.addAttribute("accounts", accounts);

        // Step 4: Load transactions
        List<Transaction> allTransactions = new ArrayList<>();
        for (Account account : accounts) {
            allTransactions.addAll(transactionService
                    .getTransactionsByAccountNumber(
                            account.getAccountNumber()));
        }
        allTransactions.sort((a, b) ->
                b.getCreatedAt().compareTo(a.getCreatedAt()));
        model.addAttribute("transactions", allTransactions);

        // Step 5: Load KYC and calculate status
        Optional<KycDocument> kyc = kycService
                .getKycByCustomerId(customer.getCustomerId());

        if (kyc.isPresent()) {
            model.addAttribute("kyc", kyc.get());

            LocalDateTime submittedAt = kyc.get().getSubmittedAt();
            LocalDateTime now = LocalDateTime.now();
            long monthsAgo = ChronoUnit.MONTHS.between(submittedAt, now);

            if (monthsAgo < 6) {
                model.addAttribute("kycStatusColor", "green");
                model.addAttribute("kycStatusMessage", "KYC Valid ✅");
                model.addAttribute("kycExpired", false);
            } else if (monthsAgo < 12) {
                model.addAttribute("kycStatusColor", "yellow");
                model.addAttribute("kycStatusMessage",
                        "KYC Expiring Soon ⚠️");
                model.addAttribute("kycExpired", false);
            } else {
                model.addAttribute("kycStatusColor", "red");
                model.addAttribute("kycStatusMessage",
                        "KYC Expired — Urgent Action Required 🔴");
                model.addAttribute("kycExpired", true);
            }
        } else {
            model.addAttribute("kyc", null);
            model.addAttribute("kycStatusColor", "red");
            model.addAttribute("kycStatusMessage",
                    "KYC Not Submitted — Action Required 🔴");
            model.addAttribute("kycExpired", true);
        }

        return "customer/dashboard";
    }
}