package com.example.carmmunity_capstone.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {

    @GetMapping("/")
    public String login() {
        return "html/start";
    }

    @GetMapping("/signUp")
    public String signUp() {
        return "html/signUp";
    }

    @GetMapping("/carTest")
    public String carTest() {
        return "html/index";
    }

    @GetMapping("/id-find")
    public String idFind() {
        return "html/id-find";
    }

    @GetMapping("/main")
    public String minPage() {
        return "html/main";
    }

    @GetMapping("/postList")
    public String postList() {
        return "html/postList";
    }

    @GetMapping("/postDetail")
    public String postDetail() {
        return "html/postDetail";
    }
}