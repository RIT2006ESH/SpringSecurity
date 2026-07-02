package net.java.spring_security.controller;

import net.java.spring_security.banking.model.KycDocument;
import net.java.spring_security.banking.service.KycService;
import net.java.spring_security.model.User;
import net.java.spring_security.service.AccountApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/manager")
public class ManagerDashboardController {

    @Autowired
    private AccountApprovalService accountApprovalService;

    @Autowired
    private KycService kycService;

    @GetMapping("/dashboard")
    public String managerDashboard(Model model) {

        // Pending accounts
        List<User> pendingAccounts =
                accountApprovalService.getPendingAccounts();
        model.addAttribute("pendingAccounts", pendingAccounts);

        // Approved accounts
        List<User> approvedAccounts =
                accountApprovalService
                        .getAccountsByStatus(User.AccountStatus.APPROVED);
        model.addAttribute("approvedAccounts", approvedAccounts);

        // Frozen accounts
        List<User> frozenAccounts =
                accountApprovalService
                        .getAccountsByStatus(User.AccountStatus.FROZEN);
        model.addAttribute("frozenAccounts", frozenAccounts);

        // Pending KYC
        List<KycDocument> pendingKyc = kycService.getPendingKyc();
        model.addAttribute("pendingKyc", pendingKyc);

        // All KYC
        List<KycDocument> allKyc = kycService.getAllKyc();
        model.addAttribute("allKyc", allKyc);

        // Counts
        model.addAttribute("pendingCount", pendingAccounts.size());
        model.addAttribute("approvedCount", approvedAccounts.size());
        model.addAttribute("frozenCount", frozenAccounts.size());
        model.addAttribute("pendingKycCount", pendingKyc.size());

        return "manager/dashboard";
    }
}