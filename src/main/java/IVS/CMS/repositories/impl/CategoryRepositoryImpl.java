package IVS.CMS.repositories.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import IVS.CMS.domain.PostCategory;
import IVS.CMS.repositories.CategoryRepository;
import IVS.CMS.repositories.rowMapper.CategoryRowMapper;

@Repository
public class CategoryRepositoryImpl implements CategoryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final CategoryRowMapper mapperDb;

    public CategoryRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate, CategoryRowMapper mapperDb) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapperDb = mapperDb;
    }

    @Override
    public PostCategory save(PostCategory category) {
        if (category.getCategoryId() == null || category.getCategoryId() == 0) {
            String sql = """
                    INSERT INTO categories (category_name, created_at, created_by, last_updated_at, last_updated_by)
                    VALUES (:categoryName, :createdAt, :createdBy, :lastUpdatedAt, :lastUpdatedBy)
                    """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(sql, mapperDb.toParams(category), keyHolder, new String[] { "category_id" });

            if (keyHolder.getKey() != null) {
                category.setCategoryId(keyHolder.getKey().longValue());
            }
        } else {
            String sql = """
                    UPDATE categories
                    SET category_name = :categoryName,
                        last_updated_at = :lastUpdatedAt,
                        last_updated_by = :lastUpdatedBy
                    WHERE category_id = :id
                    """;
            jdbcTemplate.update(sql, mapperDb.toParams(category));
        }
        return category;
    }

    @Override
    public Optional<PostCategory> findById(long id) {
        String sql = """
                SELECT category_id, category_name, created_at, created_by, last_updated_at, last_updated_by
                FROM categories
                WHERE category_id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        return jdbcTemplate.query(sql, params, mapperDb).stream().findFirst();
    }

    @Override
    public List<PostCategory> findAll() {
        String sql = """
                SELECT category_id, category_name, created_at, created_by, last_updated_at, last_updated_by
                FROM categories
                ORDER BY category_name ASC
                """;
        return jdbcTemplate.query(sql, mapperDb);
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM categories WHERE category_id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

    @Override
    public boolean existsByName(String categoryName) {
        String sql = "SELECT COUNT(1) FROM categories WHERE category_name = :categoryName";
        MapSqlParameterSource params = new MapSqlParameterSource("categoryName", categoryName);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByNameForUpdate(long id, String categoryName) {
        String sql = "SELECT COUNT(1) FROM categories WHERE category_name = :categoryName AND category_id != :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("categoryName", categoryName)
                .addValue("id", id);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }
}