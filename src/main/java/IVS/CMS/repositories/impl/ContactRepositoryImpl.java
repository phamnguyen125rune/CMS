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
        Long categoryId = findOrCreateFormCategory(contact.getService());
        String sql = """
                INSERT INTO form_details (
                    form_code,
                    full_name,
                    email,
                    phone_number,
                    company,
                    form_category_id,
                    message,
                    status,
                    reply_message,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "FORM-" + System.currentTimeMillis());
            ps.setString(2, contact.getName());
            ps.setString(3, contact.getEmail());
            ps.setString(4, contact.getPhone());
            ps.setString(5, contact.getCompany());
            ps.setLong(6, categoryId);
            ps.setString(7, contact.getMessage());
            ps.setString(8, normalizeStatus(contact.getStatus()));
            ps.setString(9, contact.getReplyMessage());
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
        String sql = baseSelect() + " WHERE fd.form_id = ?";
        List<Contact> list = jdbcTemplate.query(sql, contactRowMapper, id);
        return list.stream().findFirst();
    }

    @Override
    public List<Contact> findAll(String search, String status, int page, int size) {
        StringBuilder sql = new StringBuilder(baseSelect()).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(fd.full_name) LIKE ? OR LOWER(fd.email) LIKE ? OR LOWER(fc.category_name) LIKE ?)");
            String searchPattern = "%" + search.toLowerCase().trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            sql.append(" AND fd.status = ?");
            params.add(normalizeStatus(status));
        }

        sql.append(" ORDER BY fd.created_at DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add((page - 1) * size);

        return jdbcTemplate.query(sql.toString(), contactRowMapper, params.toArray());
    }

    @Override
    public long count(String search, String status) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM form_details fd
                LEFT JOIN form_categories fc ON fc.form_category_id = fd.form_category_id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(fd.full_name) LIKE ? OR LOWER(fd.email) LIKE ? OR LOWER(fc.category_name) LIKE ?)");
            String searchPattern = "%" + search.toLowerCase().trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            sql.append(" AND fd.status = ?");
            params.add(normalizeStatus(status));
        }

        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0;
    }

    @Override
    public void updateStatus(Long id, String status) {
        String sql = "UPDATE form_details SET status = ? WHERE form_id = ?";
        jdbcTemplate.update(sql, normalizeStatus(status), id);
    }

    @Override
    public void updateReply(Long id, String replyMessage, String status) {
        String sql = "UPDATE form_details SET reply_message = ?, status = ? WHERE form_id = ?";
        jdbcTemplate.update(sql, replyMessage, normalizeStatus(status), id);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM form_details WHERE form_id = ?";
        jdbcTemplate.update(sql, id);
    }

    private String baseSelect() {
        return """
                SELECT
                    fd.form_id AS id,
                    fd.full_name AS name,
                    fd.email,
                    fd.phone_number AS phone,
                    fd.company,
                    fc.category_name AS service,
                    fc.category_name AS subject,
                    fd.message,
                    fd.status,
                    fd.reply_message,
                    fd.created_at,
                    fd.created_at AS updated_at
                FROM form_details fd
                LEFT JOIN form_categories fc ON fc.form_category_id = fd.form_category_id
                """;
    }

    private Long findOrCreateFormCategory(String service) {
        String categoryName = service == null || service.isBlank() ? "Liên hệ" : service.trim();
        List<Long> ids = jdbcTemplate.query(
                "SELECT form_category_id FROM form_categories WHERE LOWER(category_name) = LOWER(?) LIMIT 1",
                (rs, rowNum) -> rs.getLong("form_category_id"),
                categoryName);
        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO form_categories (category_name, created_at) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, categoryName);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? "NEW" : status.trim().toUpperCase();
    }
}
