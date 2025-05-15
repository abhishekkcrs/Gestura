package com.isl.payments.payments_isl.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired JwtAuthenticationEntryPoint point;

    @Autowired JwtAuthenticationFilter filter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // ✅ Keep CSRF disabled for JWT
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/stripe/webhook").permitAll()
                        .requestMatchers("/payment/**").authenticated() // ✅ Open login endpoints
                        .anyRequest().authenticated()) // ✅ Require authentication for other endpoints
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(point)) // ✅ Custom auth failure handling
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class) // ✅ Add custom filter
                .build();
    }
}