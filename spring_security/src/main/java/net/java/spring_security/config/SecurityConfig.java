package net.java.spring_security.config;

import net.java.spring_security.security.CustomAuthenticationProvider;
import net.java.spring_security.security.JwtAuthFilter;
import net.java.spring_security.security.JwtService;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/register",
                                "/admin/register",
                                "/manager/register",
                                "/executive/register",
                                "/login",
                                "/verify-email",
                                "/verify-email/**",
                                "/api/auth/login",
                                "/css/**",
                                "/js/**",
                                "/error"
                        ).permitAll()

                        // Manager only
                        .requestMatchers(
                                "/api/approval/**",
                                "/manager/**"
                        ).hasAnyRole("MANAGER", "ADMIN")

                        // Manager and Executive
                        .requestMatchers(
                                "/api/kyc/pending",
                                "/api/kyc/all",
                                "/api/kyc/verify/**",
                                "/api/kyc/reject/**",
                                "/api/kyc/document/**",
                                "/executive/**"
                        ).hasAnyRole("MANAGER", "EXECUTIVE", "ADMIN")

                        // Customer only
                        .requestMatchers(
                                "/api/customer/accounts",
                                "/api/customer/accounts/**",
                                "/api/customer/transactions",
                                "/api/customer/deposit",
                                "/api/customer/withdraw",
                                "/api/kyc/upload",
                                "/api/kyc/status",
                                "/customer/**"
                        ).hasAnyRole("CUSTOMER", "ADMIN")

                        // Admin and staff
                        .requestMatchers(
                                "/api/customer/add",
                                "/banking/**",
                                "/dashboard"
                        ).hasAnyRole("ADMIN", "MANAGER", "EXECUTIVE")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            // Generate JWT for the authenticated user
                            String jwt = jwtService.generateToken(
                                    (UserDetails) authentication.getPrincipal());

                            // Set it as a cookie so JwtAuthFilter can read it on future requests
                            Cookie jwtCookie = new Cookie("jwt", jwt);
                            jwtCookie.setHttpOnly(true);
                            jwtCookie.setPath("/");
                            jwtCookie.setMaxAge(24 * 60 * 60); // 1 day
                            response.addCookie(jwtCookie);

                            String role = authentication.getAuthorities()
                                    .stream()
                                    .findFirst()
                                    .map(a -> a.getAuthority())
                                    .orElse("");

                            switch (role) {
                                case "ROLE_ADMIN":
                                    response.sendRedirect("/dashboard");
                                    break;
                                case "ROLE_MANAGER":
                                    response.sendRedirect("/manager/dashboard");
                                    break;
                                case "ROLE_EXECUTIVE":
                                    response.sendRedirect("/executive/dashboard");
                                    break;
                                case "ROLE_CUSTOMER":
                                    response.sendRedirect("/customer/dashboard");
                                    break;
                                default:
                                    response.sendRedirect("/login?error");
                            }
                        })
                        .failureHandler((request, response, exception) -> {
                            // Give specific feedback for lockout/expiry vs generic bad credentials
                            if (exception instanceof LockedException) {
                                response.sendRedirect("/login?locked");
                            } else if (exception instanceof CredentialsExpiredException) {
                                response.sendRedirect("/login?expired");
                            } else if (exception instanceof DisabledException) {
                                response.sendRedirect("/login?disabled");
                            } else {
                                response.sendRedirect("/login?error");
                            }
                        })
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                (request, response, authException) -> {
                                    response.setStatus(401);
                                    response.setContentType("application/json");
                                    response.getWriter().write(
                                            "{\"error\":\"Unauthorized — please login\"}");
                                },
                                new AntPathRequestMatcher("/api/**")
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .deleteCookies("jwt", "JSESSIONID")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .authenticationProvider(customAuthenticationProvider)
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}