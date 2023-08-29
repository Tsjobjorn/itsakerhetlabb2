package com.java22d.itsakerhet.Services;


import com.java22d.itsakerhet.Models.AppUser;
import com.java22d.itsakerhet.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    public AppUser registerUser(String username, String password){
        String encodedPassword= passwordEncoder.encode(password);
        return userRepository.save(new AppUser(0, username, encodedPassword));
    }
}
