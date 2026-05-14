package com.shangluhua.app.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping({"/mall", "/store", "/shop"})
    public String storefront() {
        return "forward:/index.html";
    }

    @GetMapping("/buyer")
    public String buyer() {
        return "redirect:/buyer.html";
    }
}