package com.example.contactRecordKeeper.controller;

import com.example.contactRecordKeeper.dto.ContactDTO;
import com.example.contactRecordKeeper.model.Contact;
import com.example.contactRecordKeeper.model.User;
import com.example.contactRecordKeeper.security.CurrentUser;
import com.example.contactRecordKeeper.security.UserPrincipal;
import com.example.contactRecordKeeper.service.ContactService;
import com.example.contactRecordKeeper.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;
    private final UserService userService;

    @Autowired
    public ContactController(ContactService contactService, UserService userService) {
        this.contactService = contactService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ContactDTO> createContact(@CurrentUser UserPrincipal currentUser,
                                                    @Valid @RequestBody ContactDTO contactDTO) {
        User user = userService.getUserByUsername(currentUser.getUsername());
        Contact contact = contactService.createContact(user, contactDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapContactToDTO(contact));
    }

    @GetMapping
    public ResponseEntity<List<ContactDTO>> getAllContacts(@CurrentUser UserPrincipal currentUser) {
        User user = userService.getUserByUsername(currentUser.getUsername());
        List<Contact> contacts = contactService.getContactsByUser(user);

        // Sorting contacts at controller level
        List<ContactDTO> contactDTOs = contacts.stream()
                .sorted(Comparator.comparing(Contact::getFirstName))
                .map(this::mapContactToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(contactDTOs);
    }

    @GetMapping("/{contactId}")
    public ResponseEntity<?> getContactById(@PathVariable Long contactId,
                                            @CurrentUser UserPrincipal currentUser) {
        User user = userService.getUserByUsername(currentUser.getUsername());
        Optional<Contact> contactOpt = contactService.getContactById(contactId);

        if (contactOpt.isPresent()) {
            return ResponseEntity.ok(mapContactToDTO(contactOpt.get()));
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Contact not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/{contactId}")
    public ResponseEntity<?> updateContact(@PathVariable Long contactId,
                                           @Valid @RequestBody ContactDTO contactDTO,
                                           @CurrentUser UserPrincipal currentUser) {
        User user = userService.getUserByUsername(currentUser.getUsername());
        Optional<Contact> updatedContactOpt = Optional.ofNullable(contactService.updateContact(contactId, contactDTO, user));

        if (updatedContactOpt.isPresent()) {
            return ResponseEntity.ok(mapContactToDTO(updatedContactOpt.get()));
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Contact not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Map<String, Object>> deleteContact(@PathVariable Long contactId,
                                                             @CurrentUser UserPrincipal currentUser) {
        User user = userService.getUserByUsername(currentUser.getUsername());
        boolean deleted = contactService.deleteContact(contactId);

        return deleted
                ? buildSuccessResponse("Contact deleted successfully")
                : buildErrorResponse(HttpStatus.NOT_FOUND, "Contact not found");
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteAllContacts(@CurrentUser UserPrincipal currentUser) {
        User user = userService.getUserByUsername(currentUser.getUsername());
        contactService.deleteAllContacts(user);

        return buildSuccessResponse("All contacts deleted successfully");
    }

    private ContactDTO mapContactToDTO(Contact contact) {
        ContactDTO dto = new ContactDTO();
        dto.setContactId(contact.getContactId());
        dto.setFirstName(contact.getFirstName());
        dto.setLastName(contact.getLastName());
        dto.setEmail(contact.getEmail());
        dto.setPhone(contact.getPhone());
        return dto;
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }

    private ResponseEntity<Map<String, Object>> buildSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }
}