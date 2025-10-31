package com.example.HostelManagement.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/hostel")
public class MainPageController {

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== LOGIN PAGE CONTROLLER ===");
        setNoCacheHeaders(response);
        
        // If user is already authenticated, redirect to dashboard
        if (isAuthenticated()) {
            System.out.println("User already authenticated, redirecting to dashboard");
            return "redirect:/hostel/dashboard";
        }
        
        System.out.println("User not authenticated, serving login page");
        return "forward:/adminLoginPage/AdminLogin.html";
    }

    @GetMapping("/dashboard")
    public String dashboardPage(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== DASHBOARD PAGE CONTROLLER ===");
        setNoCacheHeaders(response);
        
        // Check if user is authenticated
        if (isAuthenticated()) {
            System.out.println("User authenticated, serving dashboard");
            
            // Create or update session for dashboard
            HttpSession session = request.getSession();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof String) {
                String email = (String) auth.getPrincipal();
                session.setAttribute("authenticated", true);
                session.setAttribute("email", email);
                session.setAttribute("lastAccess", System.currentTimeMillis());
                System.out.println("Dashboard session created/updated for: " + email);
            }
            
            return "forward:/AdminDashboard/adminDashboard.html";
        } else {
            System.out.println("User not authenticated, redirecting to login");
            return "redirect:/hostel/login";
        }
    }

    @GetMapping("/registration")
    public String registrationPage(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== REGISTRATION PAGE CONTROLLER ===");
        setNoCacheHeaders(response);
        
        // If user is authenticated, redirect to dashboard
        if (isAuthenticated()) {
            System.out.println("Authenticated user trying to access registration, redirecting to dashboard");
            return "redirect:/hostel/dashboard";
        }
        
        // Check if request came from login page (referrer validation)
        String referer = request.getHeader("Referer");
        System.out.println("Registration page referer: " + referer);
        
        if (referer != null && referer.contains("/hostel/login")) {
            System.out.println("Valid referral from login page, serving registration");
            return "forward:/AdminRegistration/registration.html";
        } else {
            System.out.println("Invalid referral or direct access to registration, redirecting to login");
            return "redirect:/hostel/login";
        }
    }

    @GetMapping("/password-reset")
    public String passwordResetPage(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== PASSWORD RESET PAGE CONTROLLER ===");
        setNoCacheHeaders(response);
        
        // If user is authenticated, redirect to dashboard
        if (isAuthenticated()) {
            System.out.println("Authenticated user trying to access password reset, redirecting to dashboard");
            return "redirect:/hostel/dashboard";
        }
        
        // Check if request came from login page (referrer validation)
        String referer = request.getHeader("Referer");
        System.out.println("Password reset page referer: " + referer);
        
        if (referer != null && referer.contains("/hostel/login")) {
            System.out.println("Valid referral from login page, serving password reset");
            return "forward:/AdminPasswordReset/forgotPassword.html";
        } else {
            System.out.println("Invalid referral or direct access to password reset, redirecting to login");
            return "redirect:/hostel/login";
        }
    }

    @GetMapping("/admin-profile")
    public String adminProfilePage(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== ADMIN PROFILE PAGE CONTROLLER ===");
        setNoCacheHeaders(response);
        
        // Check if user is authenticated
        if (isAuthenticated()) {
            System.out.println("User authenticated, serving admin profile");
            return "forward:/AdminProfile/index.html";
        } else {
            System.out.println("User not authenticated, redirecting to login");
            return "redirect:/hostel/login";
        }
    }

    // Session-based logout endpoint
    // @GetMapping("/logout-session")
    // public String logoutSession(HttpServletRequest request, HttpServletResponse response) {
    //     System.out.println("=== SESSION LOGOUT ===");
        
    //     // Invalidate session
    //     HttpSession session = request.getSession(false);
    //     if (session != null) {
    //         System.out.println("Invalidating session: " + session.getId());
    //         session.invalidate();
    //     }
        
    //     // Clear security context
    //     SecurityContextHolder.clearContext();
        
    //     System.out.println("Session logout completed");
    //     return "redirect:/hostel/login";
    // }

    @RequestMapping("/")
    public String rootRedirect(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== ROOT REDIRECT ===");
        setNoCacheHeaders(response);
        
        // Redirect to dashboard if authenticated, otherwise to login
        if (isAuthenticated()) {
            return "redirect:/hostel/dashboard";
        } else {
            return "redirect:/hostel/login";
        }
    }

    // @GetMapping("/health")
    // public String healthCheck() {
    //     System.out.println("=== HEALTH CHECK ===");
    //     return "PageController is working";
    // }

    // Check session status - for debugging
    // @GetMapping("/session-status")
    // public String sessionStatus(HttpServletRequest request) {
    //     HttpSession session = request.getSession(false);
    //     StringBuilder status = new StringBuilder();
    //     status.append("=== SESSION STATUS ===\n");
        
    //     if (session != null) {
    //         status.append("Session ID: ").append(session.getId()).append("\n");
    //         status.append("Authenticated: ").append(session.getAttribute("authenticated")).append("\n");
    //         status.append("Email: ").append(session.getAttribute("email")).append("\n");
    //         status.append("Last Access: ").append(session.getAttribute("lastAccess")).append("\n");
    //     } else {
    //         status.append("No active session\n");
    //     }
        
    //     status.append("Security Context Auth: ").append(isAuthenticated()).append("\n");
        
    //     // Check authentication details
    //     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //     if (auth != null) {
    //         status.append("Auth Name: ").append(auth.getName()).append("\n");
    //         status.append("Auth Principal: ").append(auth.getPrincipal()).append("\n");
    //         status.append("Auth Details: ").append(auth.getDetails()).append("\n");
    //     }
        
    //     return status.toString();
    // }

    // Quick session check endpoint
    // @GetMapping("/check-auth")
    // public String checkAuth() {
    //     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //     if (isAuthenticated()) {
    //         return "Authenticated as: " + auth.getPrincipal();
    //     } else {
    //         return "Not authenticated";
    //     }
    // }

    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && 
               auth.isAuthenticated() && 
               !"anonymousUser".equals(auth.getPrincipal()) &&
               auth.getPrincipal() instanceof String;
    }

    private void setNoCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}