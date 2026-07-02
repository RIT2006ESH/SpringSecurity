package net.java.spring_security.banking.controller;

import net.java.spring_security.banking.model.Account;
import net.java.spring_security.banking.model.Transaction;
import net.java.spring_security.banking.service.AccountService;
import net.java.spring_security.banking.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/banking/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public String listAccounts(Model model) {
        List<Account> accounts = accountService.getAllAccounts();
        model.addAttribute("accounts", accounts);
        return "banking/accounts";
    }

    @GetMapping("/{accountNumber}")
    public String viewAccount(@PathVariable Integer accountNumber, Model model) {
        Account account = accountService.getAccountByNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        List<Transaction> transactions = transactionService
                .getTransactionsByAccountNumber(accountNumber);
        model.addAttribute("account", account);
        model.addAttribute("transactions", transactions);
        return "banking/account-detail";
    }

    @GetMapping("/new")
    public String showAddAccountForm(Model model) {
        model.addAttribute("account", new Account());
        return "banking/account-form";
    }

    @PostMapping("/save")
    public String saveAccount(@ModelAttribute Account account) {
        accountService.saveAccount(account);
        return "redirect:/banking/accounts";
    }

    @GetMapping("/delete/{accountNumber}")
    public String deleteAccount(@PathVariable Integer accountNumber) {
        accountService.deleteAccount(accountNumber);
        return "redirect:/banking/accounts";
    }
}