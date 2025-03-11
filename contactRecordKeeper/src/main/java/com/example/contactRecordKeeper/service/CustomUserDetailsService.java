package com.example.contactRecordKeeper.service;

import com.example.contactRecordKeeper.model.User;
import com.example.contactRecordKeeper.repository.UserDAO;
import com.example.contactRecordKeeper.security.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDAO userDAO;

    public CustomUserDetailsService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Search for user by username or email using UserDAO
        User user = userDAO.findByUsername(usernameOrEmail)
                .or(() -> userDAO.findByEmail(usernameOrEmail)) // Check both username and email
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

        return UserPrincipal.create(user);
    }
}