package IVS.CMS.repositories.impl;

import IVS.CMS.domain.FormCategory;
import IVS.CMS.repositories.FormCategoryRepository;
import IVS.CMS.repositories.rowMapper.FormCategoryRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class FormCategoryRepositoryImpl implements FormCategoryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final FormCategoryRowMapper rowMapper;

    public FormCategoryRepositoryImpl(JdbcTemplate jdbcTemplate, FormCategoryRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    @Override
    public FormCategory save(FormCategory category) {
        String sql = "INSERT INTO form_categories (category_name, created_at, created_by) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, category.getCategoryName());
            ps.setTimestamp(2, Timestamp.valueOf(now));
            ps.setObject(3, category.getCreatedBy());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            category.setFormCategoryId(keyHolder.getKey().longValue());
        }
        category.setCreatedAt(now);
        return category;
    }

    @Override
    public FormCategory update(FormCategory category) {
        String sql = "UPDATE form_categories SET category_name = ?, updated_at = ?, updated_by = ? WHERE form_category_id = ?";
        LocalDateTime now = LocalDateTime.now();
        
        jdbcTemplate.update(sql, 
            category.getCategoryName(), 
            Timestamp.valueOf(now), 
            category.getUpdatedBy(), 
            category.getFormCategoryId()
        );
        
        category.setUpdatedAt(now);
        return category;
    }

    @Override
    public Optional<FormCategory> findById(Long id) {
        String sql = "SELECT * FROM form_categories WHERE form_category_id = ?";
        List<FormCategory> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.stream().findFirst();
    }

    @Override
    public List<FormCategory> findAll() {
        String sql = "SELECT * FROM form_categories ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM form_categories WHERE form_category_id = ?";
        jdbcTemplate.update(sql, id);
    }
}