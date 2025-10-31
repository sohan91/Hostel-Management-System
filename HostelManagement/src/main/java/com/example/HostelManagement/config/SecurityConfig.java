package com.example.HostelManagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    private final JwtAuthFilter jwtAuthFilter;
    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Allow public endpoints
                .requestMatchers(
                    "/hostel/login",
                    "/hostel/registration", 
                    "/hostel/password-reset",
                    "/hostel/health",
                    "/api/auth/login",
                    "/api/auth/logout",
                    "/admin/check-mail",
                    "/admin/register",
                    "/hostel/api/reset-password",
                    "/css/**",
                    "/js/**", 
                    "/images/**",
                    "/assets/**",
                    "/static/**",
                    "/webjars/**",
                    "/adminLoginPage/**",
                    "/AdminRegistration/**",
                    "/AdminPasswordReset/**", 
                    "/AdminDashboard/**",
                    "/AdminProfile/**",
                    "/error"
                ).permitAll()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    System.out.println("Authentication failed for: " + request.getServletPath());
                    if (request.getServletPath().startsWith("/api/")) {
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Authentication required\"}");
                    } else {
                        response.sendRedirect("/hostel/login");
                    }
                })
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}