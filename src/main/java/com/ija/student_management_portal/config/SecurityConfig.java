package com.ija.student_management_portal.config;

import com.ija.student_management_portal.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Public – static assets served by Spring
                .requestMatchers(
                    "/", "/index.html",
                    "/assets/**", "/favicon.svg", "/icons.svg",
                    "/*.js", "/*.css", "/*.html", "/*.ico", "/*.svg", "/*.png", "/*.webp",
                    "/uploads/**"
                ).permitAll()

                // Public – auth API
                .requestMatchers("/api/auth/**").permitAll()

                // Admin-only APIs
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/bulk-import/**").hasRole("ADMIN")

                // Authenticated APIs
                .requestMatchers("/api/students/**").authenticated()
                .requestMatchers("/api/fees/**").authenticated()

                // Everything else (SPA forwarded routes) – permit so the
                // SPA can load and the React router decides what to show.
                .anyRequest().permitAll()
            )
            // No form-login – the React SPA does authentication via /api/auth/login
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())
            // Return 401 JSON for unauthenticated API calls instead of redirect
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType("application/json");
                        response.getWriter().write("{\"message\":\"Not authenticated\"}");
                    } else {
                        // For non-API requests, forward to index.html (SPA handles it)
                        request.getRequestDispatcher("/index.html").forward(request, response);
                    }
                })
            )
            .sessionManagement(session -> session
                .sessionConcurrency(concurrency -> concurrency
                    .maximumSessions(1)
                )
            );

        return http.build();
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry
                    .addResourceHandler("/uploads/**")
                    .addResourceLocations("file:./uploads/");
            }
        };
    }
}


