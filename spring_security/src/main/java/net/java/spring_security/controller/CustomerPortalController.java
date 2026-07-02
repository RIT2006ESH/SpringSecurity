package net.java.spring_security.controller;

import net.java.spring_security.banking.model.Account;
import net.java.spring_security.banking.model.Customer;
import net.java.spring_security.banking.model.KycDocument;
import net.java.spring_security.banking.model.Transaction;
import net.java.spring_security.banking.repository.AccountRepository;
import net.java.spring_security.banking.repository.CustomerRepository;
import net.java.spring_security.banking.repository.KycDocumentRepository;
import net.java.spring_security.banking.repository.TransactionRepository;
import net.java.spring_security.model.User;
import net.java.spring_security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private KycDocumentRepository kycDocumentRepository;

    @GetMapping("/dashboard")
    public String customerDashboard(Model model) {

        // Step 1: Get logged in user's email from JWT
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        String email = auth.getName();

        // Step 2: Load User from user_registration_db
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            model.addAttribute("errorMessage", "User not found");
            return "customer/dashboard";
        }
        User user = optionalUser.get();
        model.addAttribute("accountStatus", user.getAccountStatus());

        // Step 3: Find matching Customer in banking db by email
        List<Customer> customers = customerRepository.findByEmail(email);

        if (customers.isEmpty()) {
            // User registered but no matching customer record in banking db
            model.addAttribute("customer", null);
            model.addAttribute("accounts", new ArrayList<>());
            model.addAttribute("transactions", new ArrayList<>());
            model.addAttribute("kyc", null);
            return "customer/dashboard";
        }

        Customer customer = customers.get(0);
        model.addAttribute("customer", customer);

        // Step 4: Load accounts for this customer
        List<Account> accounts = accountRepository
                .findByCustomerId(customer.getCustomerId());
        model.addAttribute("accounts", accounts);

        // Step 5: Load transactions across all accounts
        List<Transaction> allTransactions = new ArrayList<>();
        for (Account account : accounts) {
            List<Transaction> txns = transactionRepository
                    .findByAccountNumber(account.getAccountNumber());
            allTransactions.addAll(txns);
        }
        // Sort by most recent first
        allTransactions.sort((a, b) ->
                b.getCreatedAt().compareTo(a.getCreatedAt()));
        model.addAttribute("transactions", allTransactions);

        // Step 6: Load KYC document
        Optional<KycDocument> kyc = kycDocumentRepository
                .findByCustomerId(customer.getCustomerId());
        model.addAttribute("kyc", kyc.orElse(null));

        return "customer/dashboard";
    }
}