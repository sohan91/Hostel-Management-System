package com.example.HostelManagement.email.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.HostelManagement.email.dao.MailDao;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class ServiceImplementation implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private final Map<String, OTPEntry> otpMap = new ConcurrentHashMap<>();

    public ServiceImplementation(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public String sendMail(MailDao dao) {
        return sendOtpEmail(dao);
    }

   private String sendOtpEmail(MailDao dao) {
    Random random = new Random();
    int otp = 100000 + random.nextInt(900000); 

    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("Email Verification <" + senderEmail + ">");
        helper.setTo(dao.getTo());
        helper.setSubject(dao.getSubject());

        String htmlContent = "<html>" +
                "<body style='font-family:Arial,sans-serif;'>" +
                "<p>Hello,</p>" +   
                "<p>You requested to verify your email for Hostel Management registration.</p>" +
                "<h3 style='color:black;'>Your OTP is: <strong>" + otp + "</strong></h3>" +
                "<p>This OTP is valid for 30 seconds. Please enter it in the registration form to verify your email.</p>" +
                "<p>If you did not request this, please ignore this email.</p>" +
                "<p>Thank you,<br>Hostel Management Team</p>" +
                "</body></html>";

        helper.setText(htmlContent, true); 

        mailSender.send(message);

        otpMap.put(dao.getTo(), new OTPEntry(String.valueOf(otp), LocalDateTime.now().plusMinutes(30)));
        return "OTP sent successfully";
    } catch (MessagingException e) {
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
