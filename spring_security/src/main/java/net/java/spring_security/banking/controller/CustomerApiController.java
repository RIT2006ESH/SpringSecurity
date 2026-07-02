package net.java.spring_security.banking.controller;

import net.java.spring_security.banking.model.Account;
import net.java.spring_security.banking.model.Customer;
import net.java.spring_security.banking.model.Transaction;
import net.java.spring_security.banking.repository.CustomerRepository;
import net.java.spring_security.banking.service.AccountService;
import net.java.spring_security.banking.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class CustomerApiController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    // ===== GET PROFILE =====
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        String email = authentication.getName();
        List<Customer> customers = customerRepository.findByEmail(email);

        if (customers.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(error("Customer profile not found for: " + email));
        }

        return ResponseEntity.ok(customers.get(0));
    }

    // ===== ADD CUSTOMER (ADMIN ONLY) =====
    @PostMapping("/add")
    public ResponseEntity<?> addCustomer(@RequestBody Customer customer,
                                         Authentication authentication) {
        try {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                return ResponseEntity.status(403)
                        .body(error("Access denied — Admin only"));
            }

            if (customer.getFirstName() == null || customer.getFirstName().isEmpty()) {
                return ResponseEntity.badRequest().body(error("First name is required"));
            }
            if (customer.getLastName() == null || customer.getLastName().isEmpty()) {
                return ResponseEntity.badRequest().body(error("Last name is required"));
            }
            if (customer.getEmail() == null || customer.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body(error("Email is required"));
            }
            if (customer.getPhone() == null || customer.getPhone().isEmpty()) {
                return ResponseEntity.badRequest().body(error("Phone is required"));
            }

            List<Customer> existing = customerRepository.findByEmail(customer.getEmail());
            if (!existing.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(error("Customer with this email already exists"));
            }

            customer.setCreatedAt(LocalDateTime.now());
            Customer saved = customerRepository.save(customer);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Customer added successfully");
            response.put("customerId", saved.getCustomerId());
            response.put("name", saved.getFirstName() + " " + saved.getLastName());
            response.put("email", saved.getEmail());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== GET MY ACCOUNTS =====
    @GetMapping("/accounts")
    public ResponseEntity<?> getMyAccounts(Authentication authentication) {
        String email = authentication.getName();
        List<Customer> customers = customerRepository.findByEmail(email);

        if (customers.isEmpty()) {
            return ResponseEntity.status(404).body(error("Customer not found"));
        }

        Integer customerId = customers.get(0).getCustomerId();
        List<Account> accounts = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }

    // ===== GET SPECIFIC ACCOUNT =====
    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<?> getAccount(@PathVariable Integer accountNumber,
                                        Authentication authentication) {
        String email = authentication.getName();
        List<Customer> customers = customerRepository.findByEmail(email);

        if (customers.isEmpty()) {
            return ResponseEntity.status(404).body(error("Customer not found"));
        }

        Integer customerId = customers.get(0).getCustomerId();
        Account account = accountService.getAccountByNumber(accountNumber).orElse(null);

        if (account == null) {
            return ResponseEntity.status(404).body(error("Account not found"));
        }

        if (!account.getCustomerId().equals(customerId)) {
            return ResponseEntity.status(403)
                    .body(error("Access denied — not your account"));
        }

        return ResponseEntity.ok(account);
    }

    // ===== GET MY TRANSACTIONS =====
    @GetMapping("/transactions")
    public ResponseEntity<?> getMyTransactions(Authentication authentication) {
        String email = authentication.getName();
        List<Customer> customers = customerRepository.findByEmail(email);

        if (customers.isEmpty()) {
            return ResponseEntity.status(404).body(error("Customer not found"));
        }

        Integer customerId = customers.get(0).getCustomerId();
        List<Account> accounts = accountService.getAccountsByCustomerId(customerId);

        Map<String, Object> response = new HashMap<>();
        for (Account account : accounts) {
            List<Transaction> transactions = transactionService
                    .getTransactionsByAccountNumber(account.getAccountNumber());
            response.put("account_" + account.getAccountNumber(), transactions);
        }

        return ResponseEntity.ok(response);
    }

    // ===== DEPOSIT =====
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody Map<String, Object> request,
                                     Authentication authentication) {
        try {
            String email = authentication.getName();
            List<Customer> customers = customerRepository.findByEmail(email);

            if (customers.isEmpty()) {
                return ResponseEntity.status(404).body(error("Customer not found"));
            }

            Integer accountNumber = (Integer) request.get("accountNumber");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            Integer customerId = customers.get(0).getCustomerId();

            Account account = accountService.getAccountByNumber(accountNumber).orElse(null);

            if (account == null) {
                return ResponseEntity.status(404).body(error("Account not found"));
            }

            if (!account.getCustomerId().equals(customerId)) {
                return ResponseEntity.status(403)
                        .body(error("Access denied — not your account"));
            }

            Transaction transaction = transactionService.deposit(accountNumber, amount);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Deposit successful");
            response.put("amount", amount);
            response.put("newBalance", accountService
                    .getAccountByNumber(accountNumber).get().getBalance());
            response.put("transactionId", transaction.getTransId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ===== WITHDRAW =====
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody Map<String, Object> request,
                                      Authentication authentication) {
        try {
            String email = authentication.getName();
            List<Customer> customers = customerRepository.findByEmail(email);

            if (customers.isEmpty()) {
                return ResponseEntity.status(404).body(error("Customer not found"));
            }

            Integer accountNumber = (Integer) request.get("accountNumber");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            Integer customerId = customers.get(0).getCustomerId();

            Account account = accountService.getAccountByNumber(accountNumber).orElse(null);

            if (account == null) {
                return ResponseEntity.status(404).body(error("Account not found"));
            }

            if (!account.getCustomerId().equals(customerId)) {
                return ResponseEntity.status(403)
                        .body(error("Access denied — not your account"));
            }

            Transaction transaction = transactionService.withdraw(accountNumber, amount);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Withdrawal successful");
            response.put("amount", amount);
            response.put("newBalance", accountService
                    .getAccountByNumber(accountNumber).get().getBalance());
            response.put("transactionId", transaction.getTransId());

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