package IVS.CMS.repositories.impl;

import IVS.CMS.domain.Tag;
import IVS.CMS.repositories.TagRepository;
import IVS.CMS.repositories.rowMapper.TagRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TagRepositoryImpl implements TagRepository {

    private final JdbcTemplate jdbcTemplate;

    public TagRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Tag> findAll() {
        String sql = "SELECT * FROM tags";
        return jdbcTemplate.query(sql, new TagRowMapper());
    }

    @Override
    public void save(Tag tag) {
        String sql = "INSERT INTO tags (tag_name, slug, created_at, created_by) VALUES (?, ?, NOW(), ?)";
        jdbcTemplate.update(sql, tag.getTagName(), tag.getSlug(), tag.getCreatedBy());
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM tags WHERE tag_id = ?";
        jdbcTemplate.update(sql, id);
    }
}