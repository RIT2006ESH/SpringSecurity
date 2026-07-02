package net.java.spring_security.controller;

import jakarta.validation.Valid;
import net.java.spring_security.dto.RegistrationRequest;
import net.java.spring_security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    // ===== CUSTOMER REGISTRATION =====
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("registrationRequest")
            RegistrationRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) return "register";

        if (!request.isPasswordMatching()) {
            model.addAttribute("errorMessage", "Passwords do not match");
            return "register";
        }

        try {
            userService.registerUser(request, "ROLE_CUSTOMER");
            return "redirect:/verify-email?email=" + request.getEmail();
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    // ===== ADMIN REGISTRATION =====
    @GetMapping("/admin/register")
    public String showAdminRegistrationForm(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest());
        model.addAttribute("formAction", "/admin/register");
        return "admin-register";
    }

    @PostMapping("/admin/register")
    public String processAdminRegistration(
            @Valid @ModelAttribute("registrationRequest")
            RegistrationRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("formAction", "/admin/register");
            return "admin-register";
        }

        if (!request.isPasswordMatching()) {
            model.addAttribute("errorMessage", "Passwords do not match");
            model.addAttribute("formAction", "/admin/register");
            return "admin-register";
        }

        try {
            userService.registerUser(request, "ROLE_ADMIN");
            return "redirect:/verify-email?email=" + request.getEmail();
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("formAction", "/admin/register");
            return "admin-register";
        }
    }

    // ===== MANAGER REGISTRATION =====
    @GetMapping("/manager/register")
    public String showManagerRegistrationForm(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest());
        model.addAttribute("formAction", "/manager/register");
        return "admin-register";
    }

    @PostMapping("/manager/register")
    public String processManagerRegistration(
            @Valid @ModelAttribute("registrationRequest")
            RegistrationRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("formAction", "/manager/register");
            return "admin-register";
        }

        if (!request.isPasswordMatching()) {
            model.addAttribute("errorMessage", "Passwords do not match");
            model.addAttribute("formAction", "/manager/register");
            return "admin-register";
        }

        try {
            userService.registerUser(request, "ROLE_MANAGER");
            return "redirect:/verify-email?email=" + request.getEmail();
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("formAction", "/manager/register");
            return "admin-register";
        }
    }

    // ===== EXECUTIVE REGISTRATION =====
    @GetMapping("/executive/register")
    public String showExecutiveRegistrationForm(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest());
        model.addAttribute("formAction", "/executive/register");
        return "admin-register";
    }

    @PostMapping("/executive/register")
    public String processExecutiveRegistration(
            @Valid @ModelAttribute("registrationRequest")
            RegistrationRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("formAction", "/executive/register");
            return "admin-register";
        }

        if (!request.isPasswordMatching()) {
            model.addAttribute("errorMessage", "Passwords do not match");
            model.addAttribute("formAction", "/executive/register");
            return "admin-register";
        }

        try {
            userService.registerUser(request, "ROLE_EXECUTIVE");
            return "redirect:/verify-email?email=" + request.getEmail();
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("formAction", "/executive/register");
            return "admin-register";
        }
    }

    // ===== EMAIL VERIFICATION =====
    @GetMapping("/verify-email")
    public String verifyEmail(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String email,
            Model model) {
        if (token != null) {
            boolean verified = userService.verifyEmail(token);
            if (verified) {
                model.addAttribute("successMessage",
                        "Email verified successfully! You can now log in.");
            } else {
                model.addAttribute("errorMessage",
                        "Invalid or expired verification link.");
            }
        } else if (email != null) {
            model.addAttribute("email", email);
        }
        return "verify-email";
    }
}