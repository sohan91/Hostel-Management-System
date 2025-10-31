package com.example.HostelManagement.email.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.HostelManagement.email.dao.MailDao;
import com.example.HostelManagement.email.service.ServiceImplementation;

@RestController
@RequestMapping("/email")
@CrossOrigin(origins = "*")
public class MailController {

    private final ServiceImplementation service;

    @Autowired
    public MailController(ServiceImplementation service) {
        this.service = service;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOTP(@RequestBody MailDao mailDao) {
        String result = service.sendMail(mailDao);
        if ("OTP sent successfully".equals(result)) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        boolean valid = service.verifyOTP(email, otp);
        if (valid) {
            return ResponseEntity.ok("OTP verified");
        } else {
    return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
    }
}
