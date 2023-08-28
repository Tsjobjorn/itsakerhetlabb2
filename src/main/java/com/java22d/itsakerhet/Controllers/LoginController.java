package com.java22d.itsakerhet.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

    @RequestMapping("/login")
    public String loginPage() {
        return "login"; // Thymeleaf template name without the ".html" extension
    }
}
