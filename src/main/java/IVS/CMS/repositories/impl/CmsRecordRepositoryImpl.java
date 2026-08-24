package IVS.CMS.repositories.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import IVS.CMS.domain.CmsRecord;
import IVS.CMS.repositories.CmsRecordRepository;
import IVS.CMS.repositories.rowMapper.CmsRecordRowMapper;

@Repository
public class CmsRecordRepositoryImpl implements CmsRecordRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final CmsRecordRowMapper rowMapper;

    public CmsRecordRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate, CmsRecordRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    @Override
    public CmsRecord save(CmsRecord record) {
        if (record.getId() == 0) {
            record.handleBeforeCreate();
            String sql = """
                    INSERT INTO cms_records (
                        module_key, title, subtitle, type, status, owner, description, image_url,
                        deleted, created_at, created_by, updated_at, updated_by
                    )
                    VALUES (
                        :moduleKey, :title, :subtitle, :type, :status, :owner, :description, :imageUrl,
                        FALSE, :createdAt, :createdBy, :updatedAt, :updatedBy
                    )
                    """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(sql, params(record), keyHolder, new String[] { "id" });
            if (keyHolder.getKey() != null) {
                record.setId(keyHolder.getKey().longValue());
            }
            return record;
        }

        record.handleUpdate();
        String sql = """
                UPDATE cms_records
                SET module_key = :moduleKey,
                    title = :title,
                    subtitle = :subtitle,
                    type = :type,
                    status = :status,
                    owner = :owner,
                    description = :description,
                    image_url = :imageUrl,
                    updated_at = :updatedAt,
                    updated_by = :updatedBy
                WHERE id = :id AND deleted = FALSE
                """;
        MapSqlParameterSource params = params(record).addValue("id", record.getId());
        jdbcTemplate.update(sql, params);
        return record;
    }

    @Override
    public Optional<CmsRecord> findById(long id) {
        String sql = "SELECT * FROM cms_records WHERE id = :id AND deleted = FALSE";
        return jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), rowMapper).stream().findFirst();
    }

    @Override
    public List<CmsRecord> findAll(String moduleKey, String search, String status, int limit, int offset) {
        String sql = baseSelect(moduleKey, search, status) + " ORDER BY updated_at DESC, id DESC LIMIT :limit OFFSET :offset";
        MapSqlParameterSource params = filterParams(moduleKey, search, status)
                .addValue("limit", limit)
                .addValue("offset", offset);
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public long count(String moduleKey, String search, String status) {
        String sql = baseCount(moduleKey, search, status);
        Long count = jdbcTemplate.queryForObject(sql, filterParams(moduleKey, search, status), Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public List<CmsRecord> findPublished(String moduleKey, int limit, int offset) {
        String sql = """
                SELECT * FROM cms_records
                WHERE deleted = FALSE
                  AND module_key = :moduleKey
                  AND status = 'PUBLISHED'
                ORDER BY updated_at DESC, id DESC
                LIMIT :limit OFFSET :offset
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("moduleKey", moduleKey)
                .addValue("limit", limit)
                .addValue("offset", offset);
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public int updateStatus(long id, String status, String updatedBy) {
        String sql = """
                UPDATE cms_records
                SET status = :status,
                    updated_at = NOW(6),
                    updated_by = :updatedBy
                WHERE id = :id AND deleted = FALSE
                """;
        return jdbcTemplate.update(sql,
                new MapSqlParameterSource("id", id).addValue("status", status).addValue("updatedBy", updatedBy));
    }

    @Override
    public int softDelete(long id, String deletedBy) {
        String sql = """
                UPDATE cms_records
                SET deleted = TRUE,
                    updated_at = NOW(6),
                    updated_by = :deletedBy
                WHERE id = :id AND deleted = FALSE
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource("id", id).addValue("deletedBy", deletedBy));
    }

    private String baseSelect(String moduleKey, String search, String status) {
        return "SELECT * FROM cms_records WHERE deleted = FALSE" + filters(moduleKey, search, status);
    }

    private String baseCount(String moduleKey, String search, String status) {
        return "SELECT COUNT(1) FROM cms_records WHERE deleted = FALSE" + filters(moduleKey, search, status);
    }

    private String filters(String moduleKey, String search, String status) {
        StringBuilder sql = new StringBuilder();
        if (!isBlank(moduleKey)) {
            sql.append(" AND module_key = :moduleKey");
        }
        if (!isBlank(status) && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND status = :status");
        }
        if (!isBlank(search)) {
            sql.append("""
                     AND (
                        LOWER(title) LIKE :search OR
                        LOWER(subtitle) LIKE :search OR
                        LOWER(type) LIKE :search OR
                        LOWER(owner) LIKE :search
                     )
                    """);
        }
        return sql.toString();
    }

    private MapSqlParameterSource filterParams(String moduleKey, String search, String status) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (!isBlank(moduleKey)) {
            params.addValue("moduleKey", moduleKey.trim());
        }
        if (!isBlank(status) && !"ALL".equalsIgnoreCase(status)) {
            params.addValue("status", status.trim().toUpperCase());
        }
        if (!isBlank(search)) {
            params.addValue("search", "%" + search.trim().toLowerCase() + "%");
        }
        return params;
    }

    private MapSqlParameterSource params(CmsRecord record) {
        return new MapSqlParameterSource()
                .addValue("moduleKey", record.getModuleKey())
                .addValue("title", record.getTitle())
                .addValue("subtitle", record.getSubtitle())
                .addValue("type", record.getType())
                .addValue("status", record.getStatus())
                .addValue("owner", record.getOwner())
                .addValue("description", record.getDescription())
                .addValue("imageUrl", record.getImageUrl())
                .addValue("createdAt", record.getCreatedAt() == null ? null : Timestamp.from(record.getCreatedAt()))
                .addValue("createdBy", record.getCreatedBy())
                .addValue("updatedAt", record.getUpdatedAt() == null ? null : Timestamp.from(record.getUpdatedAt()))
                .addValue("updatedBy", record.getUpdatedBy());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
