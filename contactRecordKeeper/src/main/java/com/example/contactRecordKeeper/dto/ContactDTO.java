package com.example.contactRecordKeeper.dto;
import com.example.contactRecordKeeper.model.User;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

import jakarta.validation.constraints.*;

@Setter
@Getter
public class ContactDTO implements Serializable {
    // Getters and Setters
    private Long contactId;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;

    @Email(message = "Email should be a valid email address")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,14}$", message = "Phone number should be valid")
    private String phone;
    private User user;

    // Constructors
    public ContactDTO() {}

    public ContactDTO(String firstName, String email) {
        this.firstName = firstName;
        this.email = email;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

    // Get Full Name
    public String getFullName() {
        return (firstName != null ? firstName : "") +
                (lastName != null ? " " + lastName : "");
    }

    // Validation Method
    public boolean isValid() {
        return firstName != null && !firstName.trim().isEmpty() &&
                (email != null || phone != null);
    }

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactDTO that = (ContactDTO) o;
        return Objects.equals(contactId, that.contactId) &&
                Objects.equals(email, that.email) &&
                Objects.equals(phone, that.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contactId, email, phone);
    }

    // ToString
    @Override
    public String toString() {
        return "ContactDTO{" +
                "contactId=" + contactId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }

    // Builder Pattern
    public static class ContactDTOBuilder {
        private ContactDTO contactDTO = new ContactDTO();

        public ContactDTOBuilder contactId(Long contactId) {
            contactDTO.contactId = contactId;
            return this;
        }

        public ContactDTOBuilder firstName(String firstName) {
            contactDTO.firstName = firstName;
            return this;
        }

        public ContactDTOBuilder lastName(String lastName) {
            contactDTO.lastName = lastName;
            return this;
        }

        public ContactDTOBuilder email(String email) {
            contactDTO.email = email;
            return this;
        }

        public ContactDTOBuilder phone(String phone) {
            contactDTO.phone = phone;
            return this;
        }

        public ContactDTO build() {
            return contactDTO;
        }
    }

    // Static method to create builder
    public static ContactDTOBuilder builder() {
        return new ContactDTOBuilder();
    }
}