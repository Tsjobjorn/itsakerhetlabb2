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
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


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

        int currentAttempt = 0;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        String line;
        try (FileInputStream fs = new FileInputStream("src/main/resources/passwords/passwords.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(fs))) {

            while ((line = br.readLine()) != null) {
                String[] stringParts = line.split("\\s+");

                LoginTask loginTask = new LoginTask(stringParts[0]);
                executorService.submit(loginTask);
                currentAttempt++;
                //System.out.println("Thread id: "+Thread.currentThread().getId());
                //int activeThreadCount = ((ThreadPoolExecutor) executorService).getActiveCount();
                //System.out.println("Active Threads: " + activeThreadCount);

                if (loginTask.isLoginSuccessful()){
                    System.out.println("Login successful for password: "+ stringParts[0]);
                    tryingToBruteForceIntoAccountUsingACommonPassword = false;
                    bruteForceService.setUserCompromised(true);
                    break;
                }

                bruteForceService.addFailedAttempt();


            }
        } catch (IOException e) {
            System.out.println("passwords.txt is probably missing!");
        }
        executorService.shutdown();
        return "redirect:/demo";
    }

    static class LoginTask implements Runnable {
        private String password;
        private boolean loginSuccessful;

        public LoginTask(String password) {
            this.password = password;
        }

        public boolean isLoginSuccessful(){
            return loginSuccessful;
        }

        @Override
        public void run(){
            String url = "http://localhost:8080/login/";
            RestTemplate restTemplate = new RestTemplate();
            org.springframework.http.HttpHeaders headers = new HttpHeaders();
            String authValue = "Basic " + Base64.getEncoder().encodeToString(("user" + ":" + password).getBytes());
            headers.set("Authorization", authValue);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()){
                loginSuccessful = true;
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
