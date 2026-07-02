package net.java.spring_security.controller;

import net.java.spring_security.banking.model.Customer;
import net.java.spring_security.banking.model.KycDocument;
import net.java.spring_security.banking.repository.CustomerRepository;
import net.java.spring_security.banking.service.KycService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/executive")
public class ExecutiveDashboardController {

    @Autowired
    private KycService kycService;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/dashboard")
    public String executiveDashboard(Model model) {

        // All customers
        List<Customer> customers = customerRepository.findAll();
        model.addAttribute("customers", customers);
        model.addAttribute("customerCount", customers.size());

        // Pending KYC
        List<KycDocument> pendingKyc = kycService.getPendingKyc();
        model.addAttribute("pendingKyc", pendingKyc);
        model.addAttribute("pendingKycCount", pendingKyc.size());

        // All KYC
        List<KycDocument> allKyc = kycService.getAllKyc();
        model.addAttribute("allKyc", allKyc);
        model.addAttribute("totalKycCount", allKyc.size());

        // Verified KYC count
        long verifiedCount = allKyc.stream()
                .filter(k -> k.getStatus() == KycDocument.KycStatus.VERIFIED)
                .count();
        model.addAttribute("verifiedKycCount", verifiedCount);

        return "executive/dashboard";
    }
}