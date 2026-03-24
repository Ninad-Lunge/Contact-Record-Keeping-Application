package com.example.contactRecordKeeper.service;

import com.example.contactRecordKeeper.dto.UserDTO;
import com.example.contactRecordKeeper.model.User;
import com.example.contactRecordKeeper.repository.UserDAO;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.contactRecordKeeper.dto.UserRegistrationDTO;
import com.example.contactRecordKeeper.exception.ResourceNotFoundException;
import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserDAO userDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(UserRegistrationDTO registrationDTO) throws BadRequestException {
        if (userDAO.existsByUsername(registrationDTO.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        if (userDAO.existsByEmail(registrationDTO.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());

        userDAO.save(user);
        return user;
    }

    public User updateUser(User currentUser, UserDTO userDTO) throws BadRequestException {
        currentUser.setFirstName(userDTO.getFirstName());
        currentUser.setLastName(userDTO.getLastName());

        if (!currentUser.getEmail().equals(userDTO.getEmail())) {
            if (userDAO.existsByEmail(userDTO.getEmail())) {
                throw new BadRequestException("Email is already registered");
            }
            currentUser.setEmail(userDTO.getEmail());
        }

        userDAO.update(currentUser);
        return currentUser;
    }

    public User getUserByEmail(String email) {
        return userDAO.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User getUserByUsername(String username) {
        return userDAO.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public void updateLastLogin(User user) {
        user.setLastLogin(LocalDateTime.now());
        userDAO.update(user);
    }
}