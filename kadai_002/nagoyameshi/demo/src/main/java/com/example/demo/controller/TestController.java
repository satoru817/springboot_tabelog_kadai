package com.example.demo.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class TestController {

    @GetMapping("/test")
    public String testRedirect(RedirectAttributes redirectAttributes) throws IOException {
        System.out.println("テストコントローラーは呼びだされています。");

        return "redirect:/auth/login?error=true";
    }


}
