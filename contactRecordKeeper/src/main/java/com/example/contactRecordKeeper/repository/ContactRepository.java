package com.example.contactRecordKeeper.repository;

import com.example.contactRecordKeeper.model.Contact;
import com.example.contactRecordKeeper.model.User;

import java.util.List;
import java.util.Optional;

public interface ContactRepository {
    Contact save(Contact contact);
    Optional<Contact> findById(Long id);
    List<Contact> findByUser(User user);
    void deleteById(Long id);
    void deleteAllByUser(User user);
}
