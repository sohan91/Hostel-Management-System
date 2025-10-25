package com.example.HostelManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPasswordResetPageController {
    
    @GetMapping("/login/password-reset")
    public String showForgotPasswordPage() {
        return "forward:/adminLoginPage/forgotPassword.html";
    }
}