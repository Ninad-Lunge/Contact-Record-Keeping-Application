package com.example.contactRecordKeeper.controller;

import com.example.contactRecordKeeper.dto.UserDTO;
import com.example.contactRecordKeeper.model.User;
import com.example.contactRecordKeeper.security.CurrentUser;
import com.example.contactRecordKeeper.security.UserPrincipal;
import com.example.contactRecordKeeper.service.UserService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        User user = userService.getUserByUsername(currentUser.getUsername());
        UserDTO userDTO = mapUserToDTO(user);

        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(@CurrentUser UserPrincipal currentUser,
                                                     @Valid @RequestBody UserDTO userDTO) throws BadRequestException {
        User user = userService.getUserByUsername(currentUser.getUsername());
        User updatedUser = userService.updateUser(user, userDTO);

        return ResponseEntity.ok(mapUserToDTO(updatedUser));
    }

    private UserDTO mapUserToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());

        return dto;
    }
}