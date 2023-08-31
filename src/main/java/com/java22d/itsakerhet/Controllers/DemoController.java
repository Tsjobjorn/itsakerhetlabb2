package com.java22d.itsakerhet.Controllers;

import com.java22d.itsakerhet.Models.AppUser;
import com.java22d.itsakerhet.PasswordGenerator;
import com.java22d.itsakerhet.Repositories.UserRepository;
import com.java22d.itsakerhet.Services.BruteForceService;
import org.hibernate.dialect.unique.CreateTableUniqueDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


@Controller
@RequestMapping("/demo")
public class DemoController {

    String rawPassword = "password";


    private boolean tryingToBruteForceIntoAccountUsingACommonPassword = false;
    private PasswordGenerator passwordGenerator;

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

        //TODO Vad gör denna?
//        model.addAttribute("currentAttempt", bruteForceService.getCurrentAttemptString());
//        System.out.println("*******************************Debug: " + bruteForceService.getCurrentAttemptString());

        return "demo";
    }

    @PostMapping("/changePassword")
    @Transactional
    public String changePassword(@RequestParam String newPassword) {
        rawPassword = newPassword;

        AppUser user = userRepository.findByUsername("user").orElse(null);
        if (user != null) {
            user.setPassword(encoder.encode(newPassword));
            userRepository.save(user);
        } else {
            System.out.println("Trasig länk");
        }
        return "redirect:/demo";
    }

    @PostMapping("/generatePassword")
    public String generatePassword(@RequestParam int length, @RequestParam(defaultValue = "false") boolean requireBigLetter,
                                   @RequestParam(defaultValue = "false") boolean requireSymbol, @RequestParam(defaultValue = "false") boolean numbersOnly,
                                   @RequestParam(defaultValue = "false") boolean smallLetterOnly) {

        passwordGenerator = new PasswordGenerator();

        rawPassword = passwordGenerator.GeneratePassword(requireBigLetter, requireSymbol, numbersOnly, smallLetterOnly, length);
        AppUser user = userRepository.findByUsername("user").orElse(null);
        if (user != null) {
            user.setPassword(encoder.encode(rawPassword));
            userRepository.save(user);
        } else {
            System.out.println("Trasig länk");
        }
        return "redirect:/demo";
    }

    @PostMapping("/newPassword")
    public String newPassword() {
        System.out.println("inne i newPassword");
        try (FileInputStream fs = new FileInputStream("src/main/resources/passwords/passwords.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(fs))) {

            Random random = new Random();
            int fileLength = random.nextInt(47024);
            for (int i = 0; i < fileLength; i++) {
                br.readLine();
            }
            String[] stringParts = br.readLine().split("\\s+");
            rawPassword = stringParts[0];

            System.out.println("nytt lösenord: " + rawPassword);
        } catch (IOException e) {
            System.out.println("passwords.txt is probably missing!");
        }

        AppUser user = userRepository.findByUsername("user").orElse(null);
        if (user != null) {
            user.setPassword(encoder.encode(rawPassword));
            userRepository.save(user);
        } else {
            System.out.println("Trasig länk");
        }
        return "redirect:/demo";
    }

    @PostMapping("/startCommonPasswords")
    public String startCommonPasswords(@RequestParam int maxAttempts) {
        tryingToBruteForceIntoAccountUsingACommonPassword = true;
        int currentAttempt = 0;
        String passwordToTry;
        try (FileInputStream fs = new FileInputStream("src/main/resources/passwords/passwords.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(fs))) {

            while (tryingToBruteForceIntoAccountUsingACommonPassword && currentAttempt < maxAttempts) {
                String[] stringParts = br.readLine().split("\\s+");
                passwordToTry = stringParts[0];
                currentAttempt++;
                bruteForceService.setFailedAttempts(currentAttempt);
                System.out.println("***********************************" + bruteForceService.getFailedAttempts());

                if (bruteForceService.postUsingRestTemplate("user", passwordToTry)) {
                    System.out.println((bruteForceService.postUsingRestTemplate("user", passwordToTry)));
                    tryingToBruteForceIntoAccountUsingACommonPassword = false;
                    bruteForceService.setUserCompromised(true);
                    System.out.println("Password cracked: " + passwordToTry);
                }

            }
        } catch (IOException e) {
            System.out.println("passwords.txt is probably missing!");
        }
        return "redirect:/demo";
    }

    @PostMapping("/startCommonPasswordsWithThreads")
    public String startCommonPasswordsWithThreads(@RequestParam int numberOfThreads){
        

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        String line;
        BlockingQueue<String> passwordQueue = new LinkedBlockingQueue<>();
        try (FileInputStream fs = new FileInputStream("src/main/resources/passwords/passwords.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(fs))) {

            while ((line = br.readLine()) != null) {
                String[] stringParts = line.split("\\s+");
                passwordQueue.put(stringParts[0]);
            }
        } catch (Exception e) {
            System.out.println("passwords.txt is probably missing!");
        }

        AtomicBoolean loginSuccessFlag = new AtomicBoolean(false);
        AtomicInteger loginAttemptCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(new LoginTask(passwordQueue, loginSuccessFlag, loginAttemptCount));
        }

        while(!loginSuccessFlag.get()){
            bruteForceService.setFailedAttempts(loginAttemptCount.get());
        }

        bruteForceService.setUserCompromised(loginSuccessFlag.get());
        executorService.shutdown();
        return "redirect:/demo";
    }

    static class LoginTask implements Runnable {

        private AtomicBoolean loginSuccessful;
        private AtomicInteger loginAttempts;
        private BlockingQueue<String> passwordQueue;

        public LoginTask(BlockingQueue<String> passwordQueue, AtomicBoolean loginSuccessful, AtomicInteger loginAttempts) {
            this.loginSuccessful = loginSuccessful;
            this.loginAttempts = loginAttempts;
            this.passwordQueue = passwordQueue;
        }

        @Override
        public void run(){
            String password;
            while (true) {
                try {
                    password = passwordQueue.poll(1, TimeUnit.SECONDS); // Poll with timeout
                    loginAttempts.incrementAndGet();
                    if (password == null) {
                        // Queue is empty, no more passwords to process
                        return;
                    }
                    if (attemptLogin("user",password)) {
                        loginSuccessful.set(true);
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        private boolean attemptLogin(String username, String password){

            System.out.println("Using username: " + username + " and password: " + password);
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:8080/login/";
            System.out.println("DEBUG: Använder username: " + username + " och password: " + password);
            HttpHeaders headers = new HttpHeaders();

            // Enkodar till basic auth. Ska vi köra json behöver vi göra en ny validering av användaren.
            String authValue = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            headers.set("Authorization", authValue);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response;
            try {
                response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            } catch (HttpClientErrorException e) {
                System.out.println("Response Status: " + e.getStatusCode());
                System.out.println("Login failed due to client error"); // Debugging comment
                return false; // Login failed due to client error (likely unauthorized)
            }

            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());

            if ("Logged in".equals(response.getBody())) {
                System.out.println("Successful login attempt with password: " + password); // Debugging comment
                return true; // Successful login
            } else {
                System.out.println("Failed login attempt with password: " + password); // Debugging comment
                return false; // Login failed
            }
        }
    }

    @PostMapping("/stopCommonPasswords")
    public String stopCommonPasswords() {
        tryingToBruteForceIntoAccountUsingACommonPassword = false;
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
