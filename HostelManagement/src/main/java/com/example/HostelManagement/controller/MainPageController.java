package com.example.HostelManagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.HostelManagement.repositories.AdminAuthDAORepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/hostel")
public class MainPageController {

    private final AdminAuthDAORepository adminAuthRepo;

    @Autowired
    public MainPageController(AdminAuthDAORepository adminAuthRepo) {
        this.adminAuthRepo = adminAuthRepo;
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        // If user has active session, redirect to dashboard
        if (session != null && session.getAttribute("loggedInUser") != null) {
            System.out.println("User already logged in, redirecting to dashboard");
            return "redirect:/hostel/dashboard";
        }
        
        System.out.println("Showing login page");
        return "forward:/adminLoginPage/AdminLogin.html";
    }

    @PostMapping("/login")
    public String showLogin(HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestParam String username,
                        @RequestParam String password) {
        
        // Check existing session without creating new one
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null && existingSession.getAttribute("loggedInUser") != null) {
            return "redirect:/hostel/dashboard";
        }
        
        boolean isValidUser = adminAuthRepo.validateAdminCredentials(username, password);

        if (isValidUser) {
            // Create new session for authenticated user
            HttpSession newSession = request.getSession();
            newSession.setAttribute("loggedInUser", username);
            newSession.setMaxInactiveInterval(30 * 60); // 30 minutes
            
            String redirectUrl = (String) newSession.getAttribute("redirectAfterLogin");
            if (redirectUrl != null) {
                newSession.removeAttribute("redirectAfterLogin");
                return "redirect:" + redirectUrl;
            }
            return "redirect:/hostel/dashboard";
        } else {
            request.setAttribute("error", "Invalid username/password");
            return "forward:/adminLoginPage/AdminLogin.html";
        }
    }

    @GetMapping("/dashboard")
    public String loadAdminDashBoard(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            // Create session for redirect tracking
            HttpSession newSession = request.getSession();
            newSession.setAttribute("redirectAfterLogin", "/hostel/dashboard");
            return "redirect:/hostel/login";
        }
        return "forward:/AdminDashboard/adminDashboard.html";
    }

    @GetMapping("/registration")
    public String registration(HttpServletRequest request) {
        String header = request.getHeader("Referer");
        if(header != null && header.contains("/hostel/login")){
            return "forward:/AdminRegistration/registration.html";
        }
        return "redirect:/hostel/login"; 
    }

    @GetMapping("/password-reset")
    public String showForgotPasswordPage(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        
        if (referer != null && referer.contains("/hostel/login")) {
            return "forward:/AdminPasswordReset/forgotPassword.html";
        }
        return "redirect:/hostel/login";
    }

    // Logout endpoint - redirects to /hostel/login
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            String username = (String) session.getAttribute("loggedInUser");
            session.invalidate();
            System.out.println("User '" + username + "' logged out successfully");
        }
        
        // Directly redirect to login URL after logout
        return "redirect:/hostel/login";
    }
}