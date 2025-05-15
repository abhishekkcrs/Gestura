package com.isl.payments.payments_isl.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtHelper jwtHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = null;
        String username = null;

        // ✅ Extract token from cookies (instead of header)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("Authorization".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // If token is still null, try to extract from header
        if (token == null) {
            String requestHeader = request.getHeader("Authorization");
            if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
                token = requestHeader.substring(7);
            }
        }

        if (token != null) {
            try {
                // ✅ Extract username from token
                username = jwtHelper.getUsernameFromToken(token);
                logger.info("Extracted username from token: {}", username);

            } catch (IllegalArgumentException e) {
                logger.error("Illegal argument while fetching the username!", e);
            } catch (ExpiredJwtException e) {
                logger.error("JWT token is expired!", e);
            } catch (MalformedJwtException e) {
                logger.error("Invalid JWT token!", e);
            } catch (Exception e) {
                logger.error("Error parsing JWT token!", e);
            }
        } else {
            logger.warn("Authorization cookie not found or empty");
        }

        // ✅ If the token is valid and user is not already authenticated → Authenticate
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            boolean validateToken = jwtHelper.validateToken(token);
            if (validateToken) {

                // ✅ Create authentication object and store it in the context
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // ✅ Set session-based context for authentication persistence
                request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

                logger.info("User authenticated successfully: {}", username);

            } else {
                logger.warn("Invalid token provided");
            }
        }

        // ✅ Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
