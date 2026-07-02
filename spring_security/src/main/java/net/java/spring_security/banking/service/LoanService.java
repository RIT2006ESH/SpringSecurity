package net.java.spring_security.banking.service;

import net.java.spring_security.banking.model.Loan;
import net.java.spring_security.banking.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public List<Loan> getLoansByCustomerId(Integer customerId) {
        return loanRepository.findByCustomerId(customerId);
    }

    public Optional<Loan> getLoanById(Integer loanId) {
        return loanRepository.findById(loanId);
    }

    public Loan saveLoan(Loan loan) {
        return loanRepository.save(loan);
    }

    public void deleteLoan(Integer loanId) {
        loanRepository.deleteById(loanId);
    }
}