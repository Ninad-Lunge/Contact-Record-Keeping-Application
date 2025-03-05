package com.example.contactRecordKeeper.repository;

import com.example.contactRecordKeeper.model.Contact;
import com.example.contactRecordKeeper.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByUser(User user);
}