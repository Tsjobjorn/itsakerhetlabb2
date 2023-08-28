package com.java22d.itsakerhet.Controllers;

import com.java22d.itsakerhet.Models.AppUser;
import com.java22d.itsakerhet.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/demo")
public class DemoController {

    String rawPassword="password";


    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginController loginController;

    @GetMapping
    public String demoPage(Model model) {
        AppUser user = userRepository.findByUsername("user").orElse(null);

        model.addAttribute("user", user);
        model.addAttribute("password", rawPassword);
        int failedAttempts = loginController.getFailedLogins();
        Boolean isUserCompromised = loginController.isCompromised();



        return "demo";  //demo.html
    }

    @PostMapping("/changePassword")
    @Transactional
    public String changePassword(@RequestParam String newPassword) {
        rawPassword = newPassword;  // Uppdatera det klartextade lösenordet innan det krypteras.

        AppUser user = userRepository.findByUsername("user").orElse(null);
        if(user != null) {
            user.setPassword(encoder.encode(newPassword));
            userRepository.save(user);
        } else {
            System.out.println("Trasig länk");
        }
        return "redirect:/demo";
    }

}