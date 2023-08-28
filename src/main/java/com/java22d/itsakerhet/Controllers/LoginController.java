package com.java22d.itsakerhet.Controllers;

import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/login")
@CrossOrigin("*")
@Data
public class LoginController {

    private int failedLogins = 0;
    private boolean isCompromised = false;

    @PostMapping("/")
    public String helloUserController(Principal principal) {
        if (principal == null) {
            failedLogins++;
            return "Login failed";
        } else {
            isCompromised = true;
            return "User logged in";
        }
    }
}
