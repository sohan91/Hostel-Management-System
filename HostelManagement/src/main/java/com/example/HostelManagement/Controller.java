package com.example.HostelManagement;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class Controller {

    @GetMapping("/app")
    public static String String()
    {
         return "hello";
    }
}
