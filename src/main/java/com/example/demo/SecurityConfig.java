package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity in this demo (allows POST from curl)
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/api/**", "/login.html", "/css/**", "/js/**").permitAll() // Allow access to API and static resources
                .requestMatchers("/dashboard.html").hasRole("ADMIN") // Only admins can access dashboard
                .requestMatchers("/participant_dashboard.html").hasRole("TEILNEHMER")
                .requestMatchers("/trainer_dashboard.html", "/calendar.html").hasRole("BERUFSTRAINER")
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login") // This matches the form action in login.html
                .usernameParameter("username") // Matches the input name in login.html
                .passwordParameter("password") // Matches the input name in login.html
                .successHandler((request, response, authentication) -> {
                    var roles = authentication.getAuthorities();
                    if (roles.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                        response.sendRedirect("/dashboard.html");
                    } else if (roles.stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEILNEHMER"))) {
                        response.sendRedirect("/participant_dashboard.html");
                    } else if (roles.stream().anyMatch(a -> a.getAuthority().equals("ROLE_BERUFSTRAINER"))) {
                        response.sendRedirect("/trainer_dashboard.html");
                    } else {
                        response.sendRedirect("/api/all");
                    }
                })
                .permitAll()
            )
            .logout((logout) -> logout.permitAll());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
