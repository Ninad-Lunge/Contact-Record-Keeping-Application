package com.example.contactRecordKeeper.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

@Setter
@Getter
public class UserRegistrationDTO implements Serializable {
    // Getters and Setters
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    private String firstName;
    private String lastName;

    // Constructors
    public UserRegistrationDTO() {}

    public UserRegistrationDTO(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Validation Methods
    public boolean isPasswordValid() {
        return password != null && password.length() >= 6;
    }

    public boolean isUsernameValid() {
        return username != null && username.length() >= 3 && username.length() <= 50;
    }

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRegistrationDTO that = (UserRegistrationDTO) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email);
    }

    // ToString
    @Override
    public String toString() {
        return "UserRegistrationDTO{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    // Builder Pattern
    public static class UserRegistrationDTOBuilder {
        private UserRegistrationDTO dto = new UserRegistrationDTO();

        public UserRegistrationDTOBuilder username(String username) {
            dto.username = username;
            return this;
        }

        public UserRegistrationDTOBuilder email(String email) {
            dto.email = email;
            return this;
        }

        public UserRegistrationDTOBuilder password(String password) {
            dto.password = password;
            return this;
        }

        public UserRegistrationDTOBuilder firstName(String firstName) {
            dto.firstName = firstName;
            return this;
        }

        public UserRegistrationDTOBuilder lastName(String lastName) {
            dto.lastName = lastName;
            return this;
        }

        public UserRegistrationDTO build() {
            return dto;
        }
    }

    // Static method to create builder
    public static UserRegistrationDTOBuilder builder() {
        return new UserRegistrationDTOBuilder();
    }
}