package IVS.CMS.repositories.impl;

import IVS.CMS.domain.FormDetail;
import IVS.CMS.repositories.FormDetailRepository;
import IVS.CMS.repositories.rowMapper.FormDetailRowMapper;
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
import java.util.UUID;

@Repository
public class FormDetailRepositoryImpl implements FormDetailRepository {

    private final JdbcTemplate jdbcTemplate;
    private final FormDetailRowMapper formDetailRowMapper;

    public FormDetailRepositoryImpl(JdbcTemplate jdbcTemplate, FormDetailRowMapper formDetailRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.formDetailRowMapper = formDetailRowMapper;
    }

    @Override
    public FormDetail save(FormDetail formDetail) {
        String formCode = formDetail.getFormCode() != null ? formDetail.getFormCode() : "FORM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Bổ sung lưu gmail_message_id và gmail_thread_id
        String sql = "INSERT INTO form_details (form_code, full_name, email, phone_number, company, form_category_id, message, status, gmail_message_id, gmail_thread_id, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, formCode);
            ps.setString(2, formDetail.getFullName());
            ps.setString(3, formDetail.getEmail());
            ps.setString(4, formDetail.getPhoneNumber());
            ps.setString(5, formDetail.getCompany());
            ps.setLong(6, formDetail.getFormCategoryId());
            ps.setString(7, formDetail.getMessage());
            ps.setString(8, formDetail.getStatus());
            ps.setString(9, formDetail.getGmailMessageId());
            ps.setString(10, formDetail.getGmailThreadId());
            ps.setTimestamp(11, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            formDetail.setFormId(keyHolder.getKey().longValue());
        }
        formDetail.setFormCode(formCode);
        formDetail.setCreatedAt(now);
        return formDetail;
    }

    @Override
    public Optional<FormDetail> findById(Long id) {
        String sql = "SELECT * FROM form_details WHERE form_id = ?";
        List<FormDetail> list = jdbcTemplate.query(sql, formDetailRowMapper, id);
        return list.stream().findFirst();
    }

    @Override
    public List<FormDetail> findAll(String search, String status, int page, int size) {
        StringBuilder sql = new StringBuilder("SELECT * FROM form_details WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(full_name) LIKE ? OR LOWER(email) LIKE ? OR LOWER(form_code) LIKE ?)");
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
        
        int offset = (page - 1) * size;
        sql.append(" LIMIT ").append(offset).append(", ").append(size);

        return jdbcTemplate.query(sql.toString(), formDetailRowMapper, params.toArray());
    }

    @Override
    public long count(String search, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM form_details WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(full_name) LIKE ? OR LOWER(email) LIKE ? OR LOWER(form_code) LIKE ?)");
            String searchPattern = "%" + search.toLowerCase().trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            sql.append(" AND status = ?");
            params.add(status.toLowerCase());
        }

        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0;
    }

    @Override
    public void updateStatus(Long id, String status) {
        String sql = "UPDATE form_details SET status = ? WHERE form_id = ?";
        jdbcTemplate.update(sql, status, id);
    }

    @Override
    public void updateReply(Long id, String replyMessage, String status, String gmailMessageId, String gmailThreadId, LocalDateTime repliedAt) {
        String sql = "UPDATE form_details SET reply_message = ?, status = ?, gmail_message_id = ?, gmail_thread_id = ?, replied_at = ? WHERE form_id = ?";
        jdbcTemplate.update(
                sql,
                replyMessage,
                status,
                gmailMessageId,
                gmailThreadId,
                repliedAt != null ? Timestamp.valueOf(repliedAt) : null,
                id
        );
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM form_details WHERE form_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByGmailMessageId(String gmailMessageId) {
        if (gmailMessageId == null || gmailMessageId.isBlank()) return false;
        String sql = "SELECT COUNT(*) FROM form_details WHERE gmail_message_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, gmailMessageId);
        return count != null && count > 0;
    }
}