package com.example.HostelManagement.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
@GetMapping("/get-session-room-details")
public ResponseEntity<Map<String, Object>> getSessionRoomDetails(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("roomId", session.getAttribute("roomId"));
        sessionData.put("roomNumber", session.getAttribute("roomNumber"));
        sessionData.put("floorNumber", session.getAttribute("floorNumber"));
        sessionData.put("sharingType", session.getAttribute("sharingType"));
        sessionData.put("sharingTypeId", session.getAttribute("sharingTypeId"));
        
        return ResponseEntity.ok(sessionData);
    }
    return ResponseEntity.ok(Collections.emptyMap());
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
@GetMapping("/hosteler-list")
public String hostlerListPage(
        @RequestParam(required = false) Integer roomId,
        @RequestParam(required = false) String roomNumber,
        @RequestParam(required = false) Integer floorNumber,
        @RequestParam(required = false) String sharingType,
        @RequestParam(required = false) Integer sharingTypeId,
        HttpServletRequest request, 
        HttpServletResponse response) {
    
    System.out.println("=== HOSTLER LIST PAGE CONTROLLER ===");
    System.out.println("Room Details - ID: " + roomId + ", Number: " + roomNumber + 
                      ", Floor: " + floorNumber + ", Sharing: " + sharingType);
    
    setNoCacheHeaders(response);
    
    if (isAuthenticated()) {
        System.out.println("User authenticated, serving hostler list");
 
        HttpSession session = request.getSession();
        if (roomId != null) {
            session.setAttribute("roomId", roomId);
            session.setAttribute("roomNumber", roomNumber);
            session.setAttribute("floorNumber", floorNumber);
            session.setAttribute("sharingType", sharingType);
            session.setAttribute("sharingTypeId", sharingTypeId);
            session.setAttribute("lastAccess", System.currentTimeMillis());
            
            System.out.println("Room details stored in session for hostler list");
        }
        
        return "forward:/HostlerList/hostelerList.html";
    } else {
        System.out.println("User not authenticated, redirecting to login");
        return "redirect:/hostel/login";
    }
}
    @RequestMapping("/")
    public String rootRedirect(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== ROOT REDIRECT ===");
        setNoCacheHeaders(response);
        

        if (isAuthenticated()) {
            return "redirect:/hostel/dashboard";
        } else {
            return "redirect:/hostel/login";
        }
    }


    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && 
               auth.isAuthenticated() && 
               !"anonymousUser".equals(auth.getPrincipal()) &&
               auth.getPrincipal() instanceof String;
    }


    @GetMapping("/room-booking")
    public String bookRoom()
    {
           return "forward:/RoomBooking/roomBooking.html";
    }

    private void setNoCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}