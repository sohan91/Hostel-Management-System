package com.example.HostelManagement.controller;

import org.springframework.web.bind.annotation.*;

@org.springframework.stereotype.Controller
@RequestMapping("/hostel")
public class MainPageController {

    @GetMapping("/login")
    public String showLoginPage()
    {
         return "forward:/adminLoginPage/AdminLogin.html";
    }
}
