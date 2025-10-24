package com.example.HostelManagement.email.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.HostelManagement.email.dao.MailDao;

@Service
public class ServiceImplementation implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private final Map<String, OTPEntry> otpMap = new ConcurrentHashMap<>();

    @Autowired
    public ServiceImplementation(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public String sendMail(MailDao dao) {
        return sendOtpEmail(dao);
    }

    private String sendOtpEmail(MailDao dao) {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(dao.getTo());
        message.setSubject(dao.getSubject());
        message.setText("Your OTP is: " + otp);

        try {
            mailSender.send(message);
            otpMap.put(dao.getTo(), new OTPEntry(String.valueOf(otp), LocalDateTime.now().plusSeconds(30)));
            return "OTP sent successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to send OTP";
        }
    }

    @Override
    public boolean verifyOTP(String email, String otp) {
        OTPEntry entry = otpMap.get(email);
        if (entry != null && entry.getOtp().equals(otp) && entry.getExpire().isAfter(LocalDateTime.now())) {
            otpMap.remove(email);
            return true;
        }
        return false;
    }

    private static class OTPEntry {
        private final String otp;
        private final LocalDateTime expire;

        public OTPEntry(String otp, LocalDateTime expire) {
            this.otp = otp;
            this.expire = expire;
        }

        public String getOtp() {
            return otp;
        }

        public LocalDateTime getExpire() {
            return expire;
        }
    }
}
