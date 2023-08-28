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

    @PostMapping("/")
    public String helloUserController(Principal principal) { //FÖR TEST FRÅN POSTMAN

        return "Logged in";
    }
}
