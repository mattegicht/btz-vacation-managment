package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity in this demo (allows POST from curl)
            .authorizeHttpRequests((requests) -> requests
                // Unauthenticated access: only login page and static assets
                .requestMatchers("/login.html", "/css/**", "/js/**").permitAll()
                // Role-restricted HTML pages
                .requestMatchers("/dashboard.html").hasRole("ADMIN")
                .requestMatchers("/participant_dashboard.html").hasRole("TEILNEHMER")
                .requestMatchers("/trainer_dashboard.html", "/calendar.html").hasRole("BERUFSTRAINER")
                // Admin-only API endpoints
                .requestMatchers("/api/all", "/api/addVacation").hasRole("ADMIN")
                // Trainer (and admin) API endpoints. Trainers are additionally scoped in
                // MainController to the participants assigned to them.
                .requestMatchers(HttpMethod.DELETE, "/api/delete/**").hasAnyRole("ADMIN", "BERUFSTRAINER")
                .requestMatchers("/api/updateVacationDays", "/api/add").hasAnyRole("ADMIN", "BERUFSTRAINER")
                .requestMatchers("/api/approveVacation", "/api/denyVacation",
                        "/api/usersByTrainer", "/api/addCompanyHolidays",
                        "/api/getCompanyHolidays", "/api/removeCompanyHolidays").hasAnyRole("ADMIN", "BERUFSTRAINER")
                // Any authenticated user
                .requestMatchers("/api/me", "/api/changePassword",
                        "/api/requestVacation", "/api/vacationsByUser").authenticated()
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

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }
}
