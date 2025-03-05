package com.example.contactRecordKeeper.controller;

import com.example.contactRecordKeeper.model.User;
import com.example.contactRecordKeeper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/test")
    public String testApi() {
        return "Hello, API is working!";
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.registerUser(user);
    }
}