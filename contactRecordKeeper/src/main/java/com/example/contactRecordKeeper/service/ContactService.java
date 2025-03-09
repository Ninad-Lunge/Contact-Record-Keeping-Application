package com.example.contactRecordKeeper.service;

import com.example.contactRecordKeeper.dto.ContactDTO;
import com.example.contactRecordKeeper.model.Contact;
import com.example.contactRecordKeeper.model.User;
import com.example.contactRecordKeeper.repository.ContactDAO;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContactService {

    private final ContactDAO contactDAO;

    public ContactService(ContactDAO contactDAO) {
        this.contactDAO = contactDAO;
    }

    public Contact createContact(User user, @Valid ContactDTO contactDTO) {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setFirstName(contactDTO.getFirstName());
        contact.setLastName(contactDTO.getLastName());
        contact.setEmail(contactDTO.getEmail());
        contact.setPhone(contactDTO.getPhone());
        return contactDAO.save(contact);
    }

    public List<Contact> getContactsByUser(User user) {
        return contactDAO.findByUser(user);
    }

    public Optional<Contact> getContactById(Long id) {
        return contactDAO.findById(id);
    }

    public boolean deleteContact(Long id) {
        contactDAO.deleteById(id);
        return true;
    }

    public void deleteAllContacts(User user) {
        contactDAO.deleteAllByUser(user);
    }

    public Contact updateContact(Long contactId, ContactDTO contactDTO, User user) {
        Optional<Contact> existingContactOpt = contactDAO.findById(contactId);

        if (existingContactOpt.isPresent()) {
            Contact existingContact = existingContactOpt.get();

            if (!existingContact.getUser().equals(user)) {
                throw new RuntimeException("You are not authorized to update this contact");
            }

            existingContact.setFirstName(contactDTO.getFirstName());
            existingContact.setLastName(contactDTO.getLastName());
            existingContact.setEmail(contactDTO.getEmail());
            existingContact.setPhone(contactDTO.getPhone());

            return contactDAO.update(existingContact);
        } else {
            throw new RuntimeException("Contact not found");
        }
    }
}