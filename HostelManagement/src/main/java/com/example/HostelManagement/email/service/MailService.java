package com.example.HostelManagement.email.service;

import com.example.HostelManagement.email.dao.MailDao;

public interface MailService {
    String sendMail(MailDao dao);
    boolean verifyOTP(String email, String otp);
}
