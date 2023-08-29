package com.java22d.itsakerhet.Controllers;

import com.java22d.itsakerhet.DTO.BruteForceStatistics;
import com.java22d.itsakerhet.Services.BruteForceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/brute-force-statistics")
public class BruteForceStatsController {

    //Restcontroller som skickar tillbaka statistik om hur många misslyckade inloggningsförsök som har gjorts och om användaren är compromised eller inte.
    //Använder sig av BruteForceService för att hämta statistiken.
    //Skickar tillbaka ett BruteForceStatistics-objekt som innehåller antalet misslyckade inloggningsförsök och om användaren är compromised eller inte.
    private final BruteForceService bruteForceService;

    @Autowired
    public BruteForceStatsController(BruteForceService bruteForceService) {
        this.bruteForceService = bruteForceService;
    }

    @GetMapping
    public BruteForceStatistics getBruteForceStatistics() {
        return bruteForceService.getBruteForceStatistics();
    }
}

