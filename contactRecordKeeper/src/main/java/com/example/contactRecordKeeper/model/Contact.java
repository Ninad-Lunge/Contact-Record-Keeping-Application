package com.example.contactRecordKeeper.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "contacts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "phone"})) // Ensures unique contacts per user
public class Contact implements Serializable {
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contactId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String firstName;

    private String lastName;

//    @Column(unique = true)
    private String email;

//    @Column(unique = true, nullable = false)
    private String phone;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Default Constructor
    public Contact() {}

    // Parameterized Constructor
    public Contact(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Method to get full name
    public String getFullName() {
        return firstName + (lastName != null ? " " + lastName : "");
    }

    // Method to validate contact information
    public boolean isValid() {
        return firstName != null && !firstName.trim().isEmpty() &&
                (email != null || phone != null);
    }

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(contactId, contact.contactId) &&
                Objects.equals(email, contact.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contactId, email);
    }

    // ToString
    @Override
    public String toString() {
        return "Contact{" +
                "contactId=" + contactId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }

    // Builder pattern contact creation
    public static class ContactBuilder {
        private Contact contact = new Contact();

        public ContactBuilder firstName(String firstName) {
            contact.firstName = firstName;
            return this;
        }

        public ContactBuilder lastName(String lastName) {
            contact.lastName = lastName;
            return this;
        }

        public ContactBuilder email(String email) {
            contact.email = email;
            return this;
        }

        public ContactBuilder phone(String phone) {
            contact.phone = phone;
            return this;
        }

        public Contact build() {
            return contact;
        }
    }

    // Static method to create builder
    public static ContactBuilder builder() {
        return new ContactBuilder();
    }
}