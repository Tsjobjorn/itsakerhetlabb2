package com.java22d.itsakerhet;

import com.java22d.itsakerhet.Models.AppUser;
import com.java22d.itsakerhet.Repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class ItSakerhetLabb2Application {

    public static void main(String[] args) {
        SpringApplication.run(ItSakerhetLabb2Application.class, args);
    }


    @Bean
    CommandLineRunner run(UserRepository userRepository, PasswordEncoder passwordEncoder){
        return args -> {
            if(userRepository.findByUsername("user").isPresent()) return;


            AppUser user = new AppUser(1, "user", passwordEncoder.encode("password"));
            userRepository.save(user);
        };
    }

}
