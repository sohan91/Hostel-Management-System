package com.example.HostelManagement.controller;

import com.example.HostelManagement.repositories.AdminAuthDAORepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        // Check if already logged in
        Object user = request.getSession().getAttribute("loggedInUser");
        if (user != null) {
            return "redirect:/hostel/dashboard";
        }
        return "forward:/adminLoginPage/AdminLogin.html";
    }

    @PostMapping("/login")
    public String showLogin(HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestParam String username,
                        @RequestParam String password) {
        Object user = request.getSession().getAttribute("loggedInUser");
        if (user != null) {
            return "redirect:/hostel/dashboard";
        }
        boolean isValidUser = adminAuthRepo.validateAdminCredentials(username, password);

        if (isValidUser) {
            request.getSession().setAttribute("loggedInUser", username);
            request.getSession().setMaxInactiveInterval(30 * 60);
            
            String redirectUrl = (String) request.getSession().getAttribute("redirectAfterLogin");
            if (redirectUrl != null) {
                request.getSession().removeAttribute("redirectAfterLogin");
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
        Object user = request.getSession().getAttribute("loggedInUser");
        if (user == null) {
            request.getSession().setAttribute("redirectAfterLogin", "/hostel/dashboard");
            return "redirect:/hostel/login";
        }
        return "forward:/AdminDashboard/adminDashboard.html";
    }

    @GetMapping("/registration")
    public String registration() {
        return "forward:/AdminRegistration/registration.html";
    }
}