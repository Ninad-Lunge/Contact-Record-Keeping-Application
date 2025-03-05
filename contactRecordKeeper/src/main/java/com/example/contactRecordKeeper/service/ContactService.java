package com.example.contactRecordKeeper.service;

import com.example.contactRecordKeeper.model.Contact;
import com.example.contactRecordKeeper.model.User;
import com.example.contactRecordKeeper.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {
    @Autowired
    private ContactRepository contactRepository;

    public List<Contact> getContacts(User user) {
        return contactRepository.findByUser(user);
    }

    public Contact addContact(Contact contact) {
        return contactRepository.save(contact);
    }
}