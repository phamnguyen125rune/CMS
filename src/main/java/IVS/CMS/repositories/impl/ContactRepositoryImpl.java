package IVS.CMS.repositories.impl;

import IVS.CMS.domain.Contact;
import IVS.CMS.repositories.ContactRepository;
import IVS.CMS.repositories.rowMapper.ContactRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ContactRepositoryImpl implements ContactRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ContactRowMapper contactRowMapper;

    public ContactRepositoryImpl(JdbcTemplate jdbcTemplate, ContactRowMapper contactRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.contactRowMapper = contactRowMapper;
    }

    @Override
    public Contact save(Contact contact) {
        String sql = "INSERT INTO contacts (name, email, phone, company, service, subject, message, status, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, contact.getName());
            ps.setString(2, contact.getEmail());
            ps.setString(3, contact.getPhone());
            ps.setString(4, contact.getCompany());
            ps.setString(5, contact.getService());
            ps.setString(6, contact.getSubject());
            ps.setString(7, contact.getMessage());
            ps.setString(8, contact.getStatus());
            ps.setTimestamp(9, Timestamp.valueOf(now));
            ps.setTimestamp(10, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            contact.setId(keyHolder.getKey().longValue());
        }
        contact.setCreatedAt(now);
        contact.setUpdatedAt(now);
        return contact;
    }

    @Override
    public Optional<Contact> findById(Long id) {
        String sql = "SELECT * FROM contacts WHERE id = ?";
        List<Contact> list = jdbcTemplate.query(sql, contactRowMapper, id);
        return list.stream().findFirst();
    }

    @Override
    public List<Contact> findAll(String search, String status, int page, int size) {
        StringBuilder sql = new StringBuilder("SELECT * FROM contacts WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(email) LIKE ? OR LOWER(subject) LIKE ?)");
            String searchPattern = "%" + search.toLowerCase().trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            sql.append(" AND status = ?");
            params.add(status.toLowerCase());
        }

        sql.append(" ORDER BY created_at DESC");

        // Use direct concatenation for LIMIT/OFFSET (numeric constants, no SQL
        // injection risk)
        int offset = (page - 1) * size;
        sql.append(" LIMIT ").append(offset).append(", ").append(size);

        System.out.println("[ContactRepositoryImpl.findAll] SQL: " + sql.toString());
        System.out.println("[ContactRepositoryImpl.findAll] Params: " + params);

        return jdbcTemplate.query(sql.toString(), contactRowMapper, params.toArray());
    }

    @Override
    public long count(String search, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM contacts WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(email) LIKE ? OR LOWER(subject) LIKE ?)");
            String searchPattern = "%" + search.toLowerCase().trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            sql.append(" AND status = ?");
            params.add(status.toLowerCase());
        }

        System.out.println("[ContactRepositoryImpl.count] SQL: " + sql.toString());
        System.out.println("[ContactRepositoryImpl.count] Params: " + params);

        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0;
    }

    @Override
    public void updateStatus(Long id, String status) {
        String sql = "UPDATE contacts SET status = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, Timestamp.valueOf(LocalDateTime.now()), id);
    }

    @Override
    public void updateReply(Long id, String replyMessage, String status) {
        String sql = "UPDATE contacts SET reply_message = ?, status = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, replyMessage, status, Timestamp.valueOf(LocalDateTime.now()), id);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM contacts WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
