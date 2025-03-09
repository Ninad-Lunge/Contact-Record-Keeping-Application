package com.example.contactRecordKeeper.repository;

import com.example.contactRecordKeeper.model.Contact;
import com.example.contactRecordKeeper.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ContactRepositoryImpl implements ContactRepository {

    private final JdbcTemplate jdbcTemplate;

    public ContactRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Contact> rowMapper = (rs, rowNum) -> {
        Contact contact = new Contact();
        contact.setContactId(rs.getLong("contact_id"));
        contact.setFirstName(rs.getString("first_name"));
        contact.setLastName(rs.getString("last_name"));
        contact.setEmail(rs.getString("email"));
        contact.setPhone(rs.getString("phone"));
        return contact;
    };

    @Override
    public Contact save(Contact contact) {
        String sql = "INSERT INTO contacts (first_name, last_name, email, phone, user_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, contact.getFirstName(), contact.getLastName(), contact.getEmail(), contact.getPhone(), contact.getUser().getUserId());
        return contact;
    }

    @Override
    public Optional<Contact> findById(Long id) {
        String sql = "SELECT * FROM contacts WHERE contact_id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    @Override
    public List<Contact> findByUser(User user) {
        String sql = "SELECT * FROM contacts WHERE user_id = ?";
        return jdbcTemplate.query(sql, rowMapper, user.getUserId());
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM contacts WHERE contact_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteAllByUser(User user) {
        String sql = "DELETE FROM contacts WHERE user_id = ?";
        jdbcTemplate.update(sql, user.getUserId());
    }

    public void update(Contact contact) {
        String sql = "UPDATE contacts SET first_name = ?, last_name = ?, email = ?, phone = ?, updated_at = NOW() WHERE contact_id = ? AND user_id = ?";

        jdbcTemplate.update(sql,
                contact.getFirstName(),
                contact.getLastName(),
                contact.getEmail(),
                contact.getPhone(),
                contact.getContactId(),
                contact.getUser().getUserId()
        );
    }
}