package net.java.spring_security.config;

import net.java.spring_security.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
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
                        .failureUrl("/login?error")
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
                                new org.springframework.security.web.util
                                        .matcher.AntPathRequestMatcher("/api/**")
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .deleteCookies("jwt", "JSESSIONID")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}