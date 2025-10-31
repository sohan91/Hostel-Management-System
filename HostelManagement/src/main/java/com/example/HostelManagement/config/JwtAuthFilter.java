package com.example.HostelManagement.config;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter//
//(ensures this filter executes exactly once per HTTP request — not multiple times in the same request chain.)
{

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getServletPath();
        
        System.out.println("=== JWT FILTER ===");
        System.out.println("Path: " + path);

        // SKIP JWT VALIDATION FOR PUBLIC PATHS
        if (isPublicPath(path)) {
            System.out.println("Skipping JWT validation for public path: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        // FOR PROTECTED PATHS - VALIDATE JWT TOKEN
        String token = getJwtFromCookie(request);
        
        if (token != null && jwtUtil.validateToken(token)) {
            String email = jwtUtil.extractEmail(token);
            System.out.println("Valid token for email: " + email);
            
            // Set authentication in security context
            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            System.out.println("Authentication set successfully for: " + path);
        } else {
            System.out.println("No valid JWT token found for protected path: " + path);
        }
        
        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.equals("/hostel/login") || 
               path.equals("/api/auth/login") ||
               path.equals("/api/auth/logout") ||
               path.equals("/hostel/registration") ||
               path.equals("/hostel/password-reset") ||
               path.equals("/hostel/health") ||
               path.equals("/admin/check-mail")||
               path.equals("/admin/register")||
               path.equals("/hostel/api/**")||
               path.equals("/email/send-otp")||
               path.equals("/email/verify-otp")||
               path.equals("/hostel/api/verfigy-email")||
               path.equals("/hostel/api/reset-password")||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/assets/") ||
               path.startsWith("/static/") ||
               path.startsWith("/webjars/") ||
               path.startsWith("/adminLoginPage/") ||
               path.startsWith("/AdminRegistration/") ||
               path.startsWith("/AdminPasswordReset/") ||
               path.startsWith("/AdminDashboard/") ||
               path.startsWith("/AdminProfile/") ||
               path.equals("/error");
    }

    private String getJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}