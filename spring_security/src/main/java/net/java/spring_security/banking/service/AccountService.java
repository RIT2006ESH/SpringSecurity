package net.java.spring_security.banking.service;

import net.java.spring_security.banking.model.Account;
import net.java.spring_security.banking.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public List<Account> getAccountsByCustomerId(Integer customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    public Optional<Account> getAccountByNumber(Integer accountNumber) {
        return accountRepository.findById(accountNumber);
    }

    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    public void deleteAccount(Integer accountNumber) {
        accountRepository.deleteById(accountNumber);
    }
}