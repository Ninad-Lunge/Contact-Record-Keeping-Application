package com.example.contactRecordKeeper.dto;

import com.example.contactRecordKeeper.model.User;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Setter
@Getter
public class UserDTO implements Serializable {
    // Getters and Setters
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime lastLogin;

    // Constructors
    public UserDTO() {}

    public UserDTO(Long userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    // Get Full Name
    public String getFullName() {
        return (firstName != null ? firstName : "") +
                (lastName != null ? " " + lastName : "");
    }

    // Validation Method
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
                email != null && email.contains("@");
    }

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(userId, userDTO.userId) &&
                Objects.equals(username, userDTO.username) &&
                Objects.equals(email, userDTO.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, email);
    }

    // ToString
    @Override
    public String toString() {
        return "UserDTO{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    // Builder Pattern
    public static class UserDTOBuilder {
        private UserDTO userDTO = new UserDTO();

        public UserDTOBuilder userId(Long userId) {
            userDTO.userId = userId;
            return this;
        }

        public UserDTOBuilder username(String username) {
            userDTO.username = username;
            return this;
        }

        public UserDTOBuilder email(String email) {
            userDTO.email = email;
            return this;
        }

        public UserDTOBuilder firstName(String firstName) {
            userDTO.firstName = firstName;
            return this;
        }

        public UserDTOBuilder lastName(String lastName) {
            userDTO.lastName = lastName;
            return this;
        }

        public UserDTO build() {
            return userDTO;
        }
    }

    // Static method to create builder
    public static UserDTOBuilder builder() {
        return new UserDTOBuilder();
    }
}