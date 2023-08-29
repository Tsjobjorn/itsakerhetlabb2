package com.java22d.itsakerhet.Controllers;

import com.java22d.itsakerhet.Models.AppUser;
import com.java22d.itsakerhet.Repositories.UserRepository;
import com.java22d.itsakerhet.Services.BruteForceService;
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
    private BruteForceService bruteForceService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String demoPage(Model model) {
        AppUser user = userRepository.findByUsername("user").orElse(null);

        model.addAttribute("user", user);
        model.addAttribute("password", rawPassword);
        model.addAttribute("currentAttempt", bruteForceService.getCurrentAttemptString());

        return "demo";
    }

    @PostMapping("/changePassword")
    @Transactional
    public String changePassword(@RequestParam String newPassword) {
        rawPassword = newPassword;

        AppUser user = userRepository.findByUsername("user").orElse(null);
        if(user != null) {
            user.setPassword(encoder.encode(newPassword));
            userRepository.save(user);
        } else {
            System.out.println("Trasig länk");
        }
        return "redirect:/demo";
    }

    @PostMapping("/startIntegerBruteForce")
    public String setNumerals(@RequestParam int numerals) {
        bruteForceService.startBruteForce(numerals);
        return "redirect:/demo";
    }

    @PostMapping("/stopBruteForce")
    public String stopBruteForce() {
        bruteForceService.stopBruteForce();
        return "redirect:/demo";
    }

}
