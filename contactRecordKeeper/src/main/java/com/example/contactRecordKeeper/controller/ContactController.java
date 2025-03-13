package com.example.contactRecordKeeper.controller;

import com.example.contactRecordKeeper.dto.ContactDTO;
import com.example.contactRecordKeeper.exception.GlobalExceptionHandler;
import com.example.contactRecordKeeper.model.Contact;
import com.example.contactRecordKeeper.model.User;
import com.example.contactRecordKeeper.security.CurrentUser;
import com.example.contactRecordKeeper.security.UserPrincipal;
import com.example.contactRecordKeeper.service.ContactService;
import com.example.contactRecordKeeper.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Create a new contact", description = "Adds a new contact for the authenticated user.")
    @ApiResponse(responseCode = "201", description = "Contact created successfully")
    @PostMapping
    public ResponseEntity<?> createContact(@CurrentUser UserPrincipal currentUser,
                                           @Valid @RequestBody ContactDTO contactDTO) {
        try {
            User user = userService.getUserByUsername(currentUser.getUsername());
            Contact contact = contactService.createContact(user, contactDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapContactToDTO(contact));
        } catch (GlobalExceptionHandler.DuplicateEntryException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage())); // Return error as JSON
        }
    }

    @Operation(summary = "Get all contacts", description = "Retrieves all contacts for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Contacts retrieved successfully")
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

    @Operation(summary = "Get a contact by ID", description = "Retrieves a specific contact by ID for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contact retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Contact not found")
    })
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

    @Operation(summary = "Update a contact", description = "Updates an existing contact for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contact updated successfully"),
            @ApiResponse(responseCode = "404", description = "Contact not found")
    })
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

    @Operation(summary = "Delete a contact", description = "Deletes a specific contact by ID for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contact deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Contact not found")
    })
    @DeleteMapping("/{contactId}")
    public ResponseEntity<Map<String, Object>> deleteContact(@PathVariable Long contactId,
                                                             @CurrentUser UserPrincipal currentUser) {
        User user = userService.getUserByUsername(currentUser.getUsername());
        boolean deleted = contactService.deleteContact(contactId);

        return deleted
                ? buildSuccessResponse("Contact deleted successfully")
                : buildErrorResponse(HttpStatus.NOT_FOUND, "Contact not found");
    }

    @Operation(summary = "Delete all contacts", description = "Deletes all contacts for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "All contacts deleted successfully")
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