package com.example.HostelManagement.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.HostelManagement.dao.AdminMailExistCheckDao;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins="*")
public class AdminMailCheck {
 private final AdminMailExistCheckDao mailCheckDao;
 
 @Autowired
  public AdminMailCheck(AdminMailExistCheckDao mailCheckDao)
  {
     this.mailCheckDao = mailCheckDao;
  
    }

    @GetMapping("/check-mail")
    public Map<String,Boolean> checkMailExist(@RequestParam String email)
    {
        boolean check = mailCheckDao.emailExists(email);
         Map<String,Boolean> response = new HashMap<>();
         response.put("exists",check);
         return response; 
    }
}
