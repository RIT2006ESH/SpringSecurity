package net.java.spring_security.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import net.java.spring_security.dto.LoginRequest;
import net.java.spring_security.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(
            @ModelAttribute("loginRequest") LoginRequest request,
            Model model,
            HttpServletResponse response) {

        try {
            System.out.println("Attempting login for: " + request.getEmail());

            // Authenticate credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Load user and generate JWT
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(request.getEmail());

            System.out.println("User found: " + userDetails.getUsername()
                    + " enabled: " + userDetails.isEnabled());

            String token = jwtService.generateToken(userDetails);
            System.out.println("JWT generated: " + token);

            // Store JWT in HTTP-only cookie
            Cookie cookie = new Cookie("jwt", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(86400); // 24 hours
            response.addCookie(cookie);

            System.out.println("Cookie set, redirecting to dashboard");

            // Redirect based on role
            String role = userDetails.getAuthorities()
                    .iterator().next().getAuthority();

            if (role.equals("ROLE_MANAGER")) {
                return "redirect:/manager/dashboard";
            } else if (role.equals("ROLE_EXECUTIVE")) {
                return "redirect:/executive/dashboard";
            } else {
                return "redirect:/customer/dashboard";
            }

        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            model.addAttribute("errorMessage",
                    "Invalid email or password");
            return "login";
        }
    }
}