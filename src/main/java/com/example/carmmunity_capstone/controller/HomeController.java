package com.example.carmmunity_capstone.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class HomeController {

    @GetMapping("/")
    public String login() {
        return "/html/login";
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

    @GetMapping("/pw-find")
    public String resetPassword() {
        return "html/pw-find";
    }

    @GetMapping("/car-list")
    public String carList() {
        return "html/car-list";
    }

    @GetMapping("/parts-list")
    public String partsList() {
        return "html/parts-list";
    }

    @GetMapping("/edit-post")
    public String editPost(@RequestParam Long postId, Model model) {
        model.addAttribute("postId", postId);
        return "html/edit-post";
    }

    @GetMapping("/shopping-list")
    public String shoppingList() {
        return "html/shopping-list";
    }

    @GetMapping("/tuning-result")
    public String tuningResult() {
        return "html/tuning-result";
    }

    /**
     * 1. OAuth2 로그인 성공 시 콜백
     * (SuccessHandler가 이쪽으로 리다이렉트시킴)
     */
    @GetMapping("/oauth/callback")
    public String oauthCallback(@RequestParam(value = "accessToken", required = false) String accessToken, Model model) {

        // 1. 토큰을 View(HTML)로 전달
        model.addAttribute("accessToken", accessToken);

        // 2. 이 View(oauth-callback.html)는 localStorage에 토큰을 저장하는
        //    JavaScript 코드를 포함해야 합니다.
        return "html/oauth-callback"; // (새로 만들어야 할 HTML 파일)
    }

    /**
     * 2. OAuth2 로그인 실패 시 콜백
     * (FailureHandler가 이쪽으로 리다이렉트시킴)
     */
    @GetMapping("/oauth/error")
    public String oauthError(@RequestParam(value = "error", required = false) String error, Model model) {

        // 1. 에러 코드를 View(HTML)로 전달
        model.addAttribute("error", error);

        // 2. 에러 코드를 사용자에게 보여주는 페이지
        return "html/oauth-error"; // (새로 만들어야 할 HTML 파일)
    }

    @GetMapping("/signUp/additional-info")
    public String addtionalSignUp(@RequestParam("providerId") String providerId, @RequestParam("provider") String provider, @RequestParam("nickname") String nickname, Model model) {
        model.addAttribute("providerId", providerId);
        model.addAttribute("provider", provider);
        model.addAttribute("nickname", nickname);
        return "html/additional-signup";
    }
}