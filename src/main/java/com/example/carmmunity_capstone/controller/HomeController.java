package com.example.carmmunity_capstone.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
    public String postDetail(@RequestParam("postId") Integer postId, Model model) {
        System.out.println("postId = " + postId);
        model.addAttribute("postId", postId);
        return "html/postDetail";
    }

    @GetMapping("/postCreate")
    public String postCreate() {
        return "html/postCreate";
    }

    @GetMapping("/modifyInfo")
    public String modifyInfo() {
        return "html/modifyInfo";
    }
}