package net.java.spring_security.banking.controller;

import net.java.spring_security.banking.model.Transaction;
import net.java.spring_security.banking.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/banking/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public String listTransactions(Model model) {
        List<Transaction> transactions = transactionService.getAllTransactions();
        model.addAttribute("transactions", transactions);
        return "banking/transactions";
    }

    @GetMapping("/account/{accountNumber}")
    public String transactionsByAccount(@PathVariable Integer accountNumber,
                                        Model model) {
        List<Transaction> transactions = transactionService
                .getTransactionsByAccountNumber(accountNumber);
        model.addAttribute("transactions", transactions);
        model.addAttribute("accountNumber", accountNumber);
        return "banking/transactions";
    }

    @GetMapping("/deposit")
    public String showDepositForm(Model model) {
        model.addAttribute("accountNumber", "");
        model.addAttribute("amount", "");
        return "banking/deposit";
    }

    @PostMapping("/deposit")
    public String processDeposit(@RequestParam Integer accountNumber,
                                 @RequestParam BigDecimal amount,
                                 Model model) {
        try {
            transactionService.deposit(accountNumber, amount);
            return "redirect:/banking/accounts/" + accountNumber;
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "banking/deposit";
        }
    }

    @GetMapping("/withdraw")
    public String showWithdrawForm(Model model) {
        model.addAttribute("accountNumber", "");
        model.addAttribute("amount", "");
        return "banking/withdraw";
    }

    @PostMapping("/withdraw")
    public String processWithdraw(@RequestParam Integer accountNumber,
                                  @RequestParam BigDecimal amount,
                                  Model model) {
        try {
            transactionService.withdraw(accountNumber, amount);
            return "redirect:/banking/accounts/" + accountNumber;
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "banking/withdraw";
        }
    }
}