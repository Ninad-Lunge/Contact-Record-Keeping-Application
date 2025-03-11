package com.example.contactRecordKeeper.controller;

import com.example.contactRecordKeeper.dto.UserDTO;
import com.example.contactRecordKeeper.model.User;
import com.example.contactRecordKeeper.security.CurrentUser;
import com.example.contactRecordKeeper.security.UserPrincipal;
import com.example.contactRecordKeeper.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Get current user details", description = "Retrieves the authenticated user's details.")
    @ApiResponse(responseCode = "200", description = "User details retrieved successfully")
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        User user = userService.getUserByUsername(currentUser.getUsername());
        UserDTO userDTO = mapUserToDTO(user);

        return ResponseEntity.ok(userDTO);
    }

    @Operation(summary = "Update current user details", description = "Updates the authenticated user's details.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
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