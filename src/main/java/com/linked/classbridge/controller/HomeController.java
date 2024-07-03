package com.linked.classbridge.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String redirectToHome() {
        return "redirect:http://localhost:3000";  // 홈화면으로 리다이렉션
    }
}
