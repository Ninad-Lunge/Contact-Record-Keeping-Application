package com.example.contactRecordKeeper.repository;

import com.example.contactRecordKeeper.model.Contact;
import com.example.contactRecordKeeper.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class ContactDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public Contact save(Contact contact) {
        entityManager.persist(contact);
        return contact;
    }

    public Optional<Contact> findById(Long id) {
        Contact contact = entityManager.find(Contact.class, id);
        return Optional.ofNullable(contact);
    }

    public List<Contact> findByUser(User user) {
        return entityManager.createQuery("SELECT c FROM Contact c WHERE c.user = :user", Contact.class)
                .setParameter("user", user)
                .getResultList();
    }

    public void deleteById(Long id) {
        Contact contact = entityManager.find(Contact.class, id);
        if (contact != null) {
            entityManager.remove(contact);
        }
    }

    public void deleteAllByUser(User user) {
        entityManager.createQuery("DELETE FROM Contact c WHERE c.user = :user")
                .setParameter("user", user)
                .executeUpdate();
    }

    public Contact update(Contact contact) {
        return entityManager.merge(contact);
    }
}