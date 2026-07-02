package net.java.spring_security.banking.controller;

import net.java.spring_security.banking.model.Account;
import net.java.spring_security.banking.model.Customer;
import net.java.spring_security.banking.repository.AccountRepository;
import net.java.spring_security.banking.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/banking/search")
public class SearchController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping
    public String search(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String type,
                         Model model) {

        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);

        if (keyword != null && !keyword.isEmpty()) {

            if ("customer_id".equals(type)) {
                // Search by Customer ID
                try {
                    Integer customerId = Integer.parseInt(keyword);
                    Optional<Customer> customer =
                            customerRepository.findById(customerId);
                    List<Customer> customers = new ArrayList<>();
                    customer.ifPresent(customers::add);
                    model.addAttribute("customers", customers);
                } catch (NumberFormatException e) {
                    model.addAttribute("errorMessage",
                            "Invalid Customer ID — must be a number");
                }

            } else if ("account_number".equals(type)) {
                // Search by Account Number
                try {
                    Integer accountNumber = Integer.parseInt(keyword);
                    Optional<Account> account =
                            accountRepository.findById(accountNumber);
                    List<Account> accounts = new ArrayList<>();
                    account.ifPresent(accounts::add);
                    model.addAttribute("accounts", accounts);
                } catch (NumberFormatException e) {
                    model.addAttribute("errorMessage",
                            "Invalid Account Number — must be a number");
                }

            } else if ("mobile".equals(type)) {
                // Search by Mobile Number
                Optional<Customer> customer =
                        customerRepository.findByPhone(keyword);
                List<Customer> customers = new ArrayList<>();
                customer.ifPresent(customers::add);

                if (customers.isEmpty()) {
                    model.addAttribute("errorMessage",
                            "No customer found with mobile: " + keyword);
                }
                model.addAttribute("customers", customers);

            } else {
                // General search
                List<Customer> customers =
                        customerRepository.searchCustomers(keyword);
                List<Account> accounts =
                        accountRepository.searchAccounts(keyword);
                model.addAttribute("customers", customers);
                model.addAttribute("accounts", accounts);
            }
        }

        return "banking/search";
    }
}