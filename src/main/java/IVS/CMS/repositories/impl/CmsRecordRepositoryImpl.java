package IVS.CMS.repositories.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
        if (record.getId() > 0) {
            updateExisting(record);
            return record;
        }

        String moduleKey = normalizeModule(record.getModuleKey());
        return switch (moduleKey) {
            case "posts" -> insertPost(record);
            case "categories" -> insertCategory(record);
            case "media" -> insertMedia(record);
            case "forms" -> insertForm(record);
            case "settings" -> insertGeneralInfo(record);
            default -> transientRecord(record);
        };
    }

    @Override
    public Optional<CmsRecord> findById(long id) {
        return allRows().stream()
                .filter(record -> record.getId() == id)
                .findFirst();
    }

    @Override
    public List<CmsRecord> findAll(String moduleKey, String search, String status, int limit, int offset) {
        return allRows().stream()
                .filter(record -> matches(record, moduleKey, search, status))
                .sorted(Comparator.comparing(this::sortInstant).reversed()
                        .thenComparing(CmsRecord::getId, Comparator.reverseOrder()))
                .skip(Math.max(0, offset))
                .limit(Math.max(0, limit))
                .toList();
    }

    @Override
    public long count(String moduleKey, String search, String status) {
        return allRows().stream()
                .filter(record -> matches(record, moduleKey, search, status))
                .count();
    }

    @Override
    public List<CmsRecord> findPublished(String moduleKey, int limit, int offset) {
        return allRows().stream()
                .filter(record -> matches(record, moduleKey, null, "PUBLISHED"))
                .sorted(Comparator.comparing(this::sortInstant).reversed()
                        .thenComparing(CmsRecord::getId, Comparator.reverseOrder()))
                .skip(Math.max(0, offset))
                .limit(Math.max(0, limit))
                .toList();
    }

    @Override
    public int updateStatus(long id, String status, String updatedBy) {
        String normalizedStatus = normalizeStatus(status);
        int affected = jdbcTemplate.update(
                "UPDATE posts SET status = LOWER(:status), updated_at = NOW(6) WHERE post_id = :id",
                new MapSqlParameterSource("id", id).addValue("status", normalizedStatus));
        affected += jdbcTemplate.update(
                "UPDATE form_details SET status = :status WHERE form_id = :id",
                new MapSqlParameterSource("id", id).addValue("status", normalizedStatus));
        return affected;
    }

    @Override
    public int softDelete(long id, String deletedBy) {
        int affected = jdbcTemplate.update(
                "UPDATE posts SET status = 'deleted', updated_at = NOW(6) WHERE post_id = :id",
                new MapSqlParameterSource("id", id));
        affected += jdbcTemplate.update(
                "DELETE FROM form_details WHERE form_id = :id",
                new MapSqlParameterSource("id", id));
        return affected;
    }

    private List<CmsRecord> allRows() {
        List<CmsRecord> records = new ArrayList<>();
        records.addAll(queryRows("""
                SELECT
                    p.post_id AS id,
                    'posts' AS module_key,
                    p.title,
                    p.slug AS subtitle,
                    'Bài viết' AS type,
                    UPPER(p.status) AS status,
                    CAST(p.created_by AS CHAR) AS owner,
                    p.summary AS description,
                    NULL AS image_url,
                    p.status = 'deleted' AS deleted,
                    p.created_at,
                    CAST(p.created_by AS CHAR) AS created_by,
                    p.updated_at,
                    CAST(p.updated_by AS CHAR) AS updated_by
                FROM posts p
                """));
        records.addAll(queryRows("""
                SELECT
                    pc.category_id AS id,
                    'categories' AS module_key,
                    pc.category_name AS title,
                    pc.slug AS subtitle,
                    'Danh mục' AS type,
                    'ACTIVE' AS status,
                    CAST(pc.created_by AS CHAR) AS owner,
                    pc.category_name AS description,
                    NULL AS image_url,
                    FALSE AS deleted,
                    pc.created_at,
                    CAST(pc.created_by AS CHAR) AS created_by,
                    pc.updated_at,
                    CAST(pc.updated_by AS CHAR) AS updated_by
                FROM post_categories pc
                """));
        records.addAll(queryRows("""
                SELECT
                    ml.media_id AS id,
                    'media' AS module_key,
                    ml.file_name AS title,
                    CAST(ml.file_size AS CHAR) AS subtitle,
                    ml.file_type AS type,
                    'READY' AS status,
                    CAST(ml.created_by AS CHAR) AS owner,
                    ml.mime_type AS description,
                    ml.file_path AS image_url,
                    FALSE AS deleted,
                    ml.created_at,
                    CAST(ml.created_by AS CHAR) AS created_by,
                    ml.updated_at,
                    CAST(ml.updated_by AS CHAR) AS updated_by
                FROM media_library ml
                """));
        records.addAll(queryRows("""
                SELECT
                    fd.form_id AS id,
                    'forms' AS module_key,
                    fd.full_name AS title,
                    fd.email AS subtitle,
                    fc.category_name AS type,
                    UPPER(fd.status) AS status,
                    fd.company AS owner,
                    fd.message AS description,
                    NULL AS image_url,
                    FALSE AS deleted,
                    fd.created_at,
                    NULL AS created_by,
                    fd.created_at AS updated_at,
                    NULL AS updated_by
                FROM form_details fd
                LEFT JOIN form_categories fc ON fc.form_category_id = fd.form_category_id
                """));
        records.addAll(queryRows("""
                SELECT
                    r.role_id AS id,
                    'roles' AS module_key,
                    r.role_name AS title,
                    r.role_description AS subtitle,
                    'Role' AS type,
                    CASE WHEN r.is_active THEN 'ACTIVE' ELSE 'LOCKED' END AS status,
                    CAST(r.created_by AS CHAR) AS owner,
                    r.role_description AS description,
                    NULL AS image_url,
                    FALSE AS deleted,
                    r.created_at,
                    CAST(r.created_by AS CHAR) AS created_by,
                    r.updated_at,
                    CAST(r.updated_by AS CHAR) AS updated_by
                FROM roles r
                """));
        records.addAll(queryRows("""
                SELECT
                    u.user_id AS id,
                    'profile' AS module_key,
                    u.full_name AS title,
                    u.email AS subtitle,
                    COALESCE(r.role_name, 'User') AS type,
                    CASE WHEN u.is_active THEN 'ACTIVE' ELSE 'LOCKED' END AS status,
                    CAST(u.created_by AS CHAR) AS owner,
                    u.address AS description,
                    u.avatar_url AS image_url,
                    u.deleted_at IS NOT NULL AS deleted,
                    u.created_at,
                    CAST(u.created_by AS CHAR) AS created_by,
                    u.updated_at,
                    CAST(u.updated_by AS CHAR) AS updated_by
                FROM users u
                LEFT JOIN roles r ON r.role_id = u.role_id
                """));
        records.addAll(queryRows("""
                SELECT
                    gi.general_info_id AS id,
                    'settings' AS module_key,
                    gi.company_name AS title,
                    gi.website_name AS subtitle,
                    'General' AS type,
                    'ACTIVE' AS status,
                    gi.email AS owner,
                    gi.website_description AS description,
                    gi.logo AS image_url,
                    FALSE AS deleted,
                    gi.created_at,
                    CAST(gi.created_by AS CHAR) AS created_by,
                    gi.updated_at,
                    CAST(gi.updated_by AS CHAR) AS updated_by
                FROM general_info gi
                """));
        records.addAll(queryRows("""
                SELECT
                    al.log_id AS id,
                    'logs' AS module_key,
                    al.action AS title,
                    al.entity_type AS subtitle,
                    'Audit' AS type,
                    CAST(al.status_code AS CHAR) AS status,
                    CAST(al.user_id AS CHAR) AS owner,
                    al.new_value AS description,
                    NULL AS image_url,
                    FALSE AS deleted,
                    al.created_at,
                    CAST(al.user_id AS CHAR) AS created_by,
                    al.created_at AS updated_at,
                    CAST(al.user_id AS CHAR) AS updated_by
                FROM audit_logs al
                """));
        return records;
    }

    private List<CmsRecord> queryRows(String sql) {
        return jdbcTemplate.query(sql, new MapSqlParameterSource(), rowMapper);
    }

    private boolean matches(CmsRecord record, String moduleKey, String search, String status) {
        if (record == null || Boolean.TRUE.equals(record.getDeleted())) {
            return false;
        }
        if (!isBlank(moduleKey) && !normalizeModule(moduleKey).equalsIgnoreCase(record.getModuleKey())) {
            return false;
        }
        if (!isBlank(status) && !"ALL".equalsIgnoreCase(status)
                && !normalizeStatus(status).equalsIgnoreCase(record.getStatus())) {
            return false;
        }
        if (isBlank(search)) {
            return true;
        }
        String needle = search.trim().toLowerCase(Locale.ROOT);
        return contains(record.getTitle(), needle)
                || contains(record.getSubtitle(), needle)
                || contains(record.getType(), needle)
                || contains(record.getOwner(), needle);
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private Instant sortInstant(CmsRecord record) {
        if (record.getUpdatedAt() != null) {
            return record.getUpdatedAt();
        }
        if (record.getCreatedAt() != null) {
            return record.getCreatedAt();
        }
        return Instant.EPOCH;
    }

    private void updateExisting(CmsRecord record) {
        String moduleKey = normalizeModule(record.getModuleKey());
        MapSqlParameterSource params = recordParams(record);
        switch (moduleKey) {
            case "posts" -> jdbcTemplate.update("""
                    UPDATE posts
                    SET title = :title,
                        summary = :description,
                        status = LOWER(:status),
                        updated_at = NOW(6)
                    WHERE post_id = :id
                    """, params);
            case "categories" -> jdbcTemplate.update("""
                    UPDATE post_categories
                    SET category_name = :title,
                        updated_at = NOW(6)
                    WHERE category_id = :id
                    """, params);
            case "forms" -> jdbcTemplate.update("""
                    UPDATE form_details
                    SET full_name = :title,
                        email = COALESCE(:subtitle, email),
                        message = :description,
                        status = :status
                    WHERE form_id = :id
                    """, params);
            case "settings" -> jdbcTemplate.update("""
                    UPDATE general_info
                    SET company_name = :title,
                        website_name = :subtitle,
                        website_description = :description,
                        updated_at = NOW(6)
                    WHERE general_info_id = :id
                    """, params);
            default -> {
            }
        }
    }

    private CmsRecord insertPost(CmsRecord record) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update("""
                INSERT INTO posts (
                    title,
                    slug,
                    summary,
                    content,
                    status,
                    created_at,
                    created_by,
                    updated_at
                )
                VALUES (
                    :title,
                    :slug,
                    :description,
                    :description,
                    LOWER(:status),
                    NOW(6),
                    1,
                    NOW(6)
                )
                """, recordParams(record).addValue("slug", slug(record.getSubtitle(), record.getTitle())),
                keyHolder, new String[] { "post_id" });
        applyGeneratedId(record, keyHolder);
        return record;
    }

    private CmsRecord insertCategory(CmsRecord record) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update("""
                INSERT INTO post_categories (
                    category_name,
                    slug,
                    created_at,
                    updated_at
                )
                VALUES (
                    :title,
                    :slug,
                    NOW(6),
                    NOW(6)
                )
                """, recordParams(record).addValue("slug", slug(record.getSubtitle(), record.getTitle())),
                keyHolder, new String[] { "category_id" });
        applyGeneratedId(record, keyHolder);
        return record;
    }

    private CmsRecord insertMedia(CmsRecord record) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update("""
                INSERT INTO media_library (
                    file_name,
                    upload_file_name,
                    file_path,
                    mime_type,
                    file_type,
                    file_size,
                    created_at,
                    updated_at
                )
                VALUES (
                    :title,
                    :title,
                    :imageUrl,
                    'application/octet-stream',
                    COALESCE(:type, 'file'),
                    0,
                    NOW(6),
                    NOW(6)
                )
                """, recordParams(record), keyHolder, new String[] { "media_id" });
        applyGeneratedId(record, keyHolder);
        return record;
    }

    private CmsRecord insertForm(CmsRecord record) {
        Long categoryId = findOrCreateFormCategory(record.getType());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update("""
                INSERT INTO form_details (
                    form_code,
                    full_name,
                    email,
                    phone_number,
                    company,
                    form_category_id,
                    message,
                    status,
                    created_at
                )
                VALUES (
                    :formCode,
                    :title,
                    COALESCE(:subtitle, 'guest@example.com'),
                    '0000000000',
                    :owner,
                    :categoryId,
                    :description,
                    :status,
                    NOW(6)
                )
                """, recordParams(record)
                .addValue("formCode", "FORM-" + System.currentTimeMillis())
                .addValue("categoryId", categoryId), keyHolder, new String[] { "form_id" });
        applyGeneratedId(record, keyHolder);
        return record;
    }

    private CmsRecord insertGeneralInfo(CmsRecord record) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update("""
                INSERT INTO general_info (
                    logo,
                    company_name,
                    website_name,
                    website_description,
                    email,
                    created_at,
                    updated_at
                )
                VALUES (
                    COALESCE(:imageUrl, '/images/default-avatar.png'),
                    :title,
                    :subtitle,
                    :description,
                    :owner,
                    NOW(6),
                    NOW(6)
                )
                """, recordParams(record), keyHolder, new String[] { "general_info_id" });
        applyGeneratedId(record, keyHolder);
        return record;
    }

    private CmsRecord transientRecord(CmsRecord record) {
        record.setCreatedAt(Instant.now());
        record.setUpdatedAt(record.getCreatedAt());
        record.setDeleted(false);
        return record;
    }

    private String unionSelect() {
        return """
                SELECT
                    p.post_id AS id,
                    'posts' AS module_key,
                    p.title,
                    p.slug AS subtitle,
                    'Bài viết' AS type,
                    UPPER(p.status) AS status,
                    CAST(p.created_by AS CHAR) AS owner,
                    p.summary AS description,
                    NULL AS image_url,
                    p.status = 'deleted' AS deleted,
                    p.created_at,
                    CAST(p.created_by AS CHAR) AS created_by,
                    p.updated_at,
                    CAST(p.updated_by AS CHAR) AS updated_by
                FROM posts p
                UNION ALL
                SELECT
                    pc.category_id AS id,
                    'categories' AS module_key,
                    pc.category_name AS title,
                    pc.slug AS subtitle,
                    'Danh mục' AS type,
                    'ACTIVE' AS status,
                    CAST(pc.created_by AS CHAR) AS owner,
                    pc.category_name AS description,
                    NULL AS image_url,
                    FALSE AS deleted,
                    pc.created_at,
                    CAST(pc.created_by AS CHAR) AS created_by,
                    pc.updated_at,
                    CAST(pc.updated_by AS CHAR) AS updated_by
                FROM post_categories pc
                UNION ALL
                SELECT
                    ml.media_id AS id,
                    'media' AS module_key,
                    ml.file_name AS title,
                    CAST(ml.file_size AS CHAR) AS subtitle,
                    ml.file_type AS type,
                    'READY' AS status,
                    CAST(ml.created_by AS CHAR) AS owner,
                    ml.mime_type AS description,
                    ml.file_path AS image_url,
                    FALSE AS deleted,
                    ml.created_at,
                    CAST(ml.created_by AS CHAR) AS created_by,
                    ml.updated_at,
                    CAST(ml.updated_by AS CHAR) AS updated_by
                FROM media_library ml
                UNION ALL
                SELECT
                    fd.form_id AS id,
                    'forms' AS module_key,
                    fd.full_name AS title,
                    fd.email AS subtitle,
                    fc.category_name AS type,
                    UPPER(fd.status) AS status,
                    fd.company AS owner,
                    fd.message AS description,
                    NULL AS image_url,
                    FALSE AS deleted,
                    fd.created_at,
                    NULL AS created_by,
                    fd.created_at AS updated_at,
                    NULL AS updated_by
                FROM form_details fd
                LEFT JOIN form_categories fc ON fc.form_category_id = fd.form_category_id
                UNION ALL
                SELECT
                    r.role_id AS id,
                    'roles' AS module_key,
                    r.role_name AS title,
                    r.role_description AS subtitle,
                    'Role' AS type,
                    CASE WHEN r.is_active THEN 'ACTIVE' ELSE 'LOCKED' END AS status,
                    CAST(r.created_by AS CHAR) AS owner,
                    r.role_description AS description,
                    NULL AS image_url,
                    FALSE AS deleted,
                    r.created_at,
                    CAST(r.created_by AS CHAR) AS created_by,
                    r.updated_at,
                    CAST(r.updated_by AS CHAR) AS updated_by
                FROM roles r
                UNION ALL
                SELECT
                    u.user_id AS id,
                    'profile' AS module_key,
                    u.full_name AS title,
                    u.email AS subtitle,
                    COALESCE(r.role_name, 'User') AS type,
                    CASE WHEN u.is_active THEN 'ACTIVE' ELSE 'LOCKED' END AS status,
                    CAST(u.created_by AS CHAR) AS owner,
                    u.address AS description,
                    u.avatar_url AS image_url,
                    u.deleted_at IS NOT NULL AS deleted,
                    u.created_at,
                    CAST(u.created_by AS CHAR) AS created_by,
                    u.updated_at,
                    CAST(u.updated_by AS CHAR) AS updated_by
                FROM users u
                LEFT JOIN roles r ON r.role_id = u.role_id
                UNION ALL
                SELECT
                    gi.general_info_id AS id,
                    'settings' AS module_key,
                    gi.company_name AS title,
                    gi.website_name AS subtitle,
                    'General' AS type,
                    'ACTIVE' AS status,
                    gi.email AS owner,
                    gi.website_description AS description,
                    gi.logo AS image_url,
                    FALSE AS deleted,
                    gi.created_at,
                    CAST(gi.created_by AS CHAR) AS created_by,
                    gi.updated_at,
                    CAST(gi.updated_by AS CHAR) AS updated_by
                FROM general_info gi
                UNION ALL
                SELECT
                    al.log_id AS id,
                    'logs' AS module_key,
                    al.action AS title,
                    al.entity_type AS subtitle,
                    'Audit' AS type,
                    CAST(al.status_code AS CHAR) AS status,
                    CAST(al.user_id AS CHAR) AS owner,
                    al.new_value AS description,
                    NULL AS image_url,
                    FALSE AS deleted,
                    al.created_at,
                    CAST(al.user_id AS CHAR) AS created_by,
                    al.created_at AS updated_at,
                    CAST(al.user_id AS CHAR) AS updated_by
                FROM audit_logs al
                """;
    }

    private String filters(String moduleKey, String search, String status) {
        StringBuilder sql = new StringBuilder(" AND x.deleted = FALSE");
        if (!isBlank(moduleKey)) {
            sql.append(" AND x.module_key = :moduleKey");
        }
        if (!isBlank(status) && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND x.status = :status");
        }
        if (!isBlank(search)) {
            sql.append("""
                     AND (
                        LOWER(x.title) LIKE :search OR
                        LOWER(x.subtitle) LIKE :search OR
                        LOWER(x.type) LIKE :search OR
                        LOWER(x.owner) LIKE :search
                     )
                    """);
        }
        return sql.toString();
    }

    private MapSqlParameterSource filterParams(String moduleKey, String search, String status) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (!isBlank(moduleKey)) {
            params.addValue("moduleKey", normalizeModule(moduleKey));
        }
        if (!isBlank(status) && !"ALL".equalsIgnoreCase(status)) {
            params.addValue("status", normalizeStatus(status));
        }
        if (!isBlank(search)) {
            params.addValue("search", "%" + search.trim().toLowerCase() + "%");
        }
        return params;
    }

    private MapSqlParameterSource recordParams(CmsRecord record) {
        return new MapSqlParameterSource()
                .addValue("id", record.getId())
                .addValue("moduleKey", normalizeModule(record.getModuleKey()))
                .addValue("title", isBlank(record.getTitle()) ? "Untitled" : record.getTitle().trim())
                .addValue("subtitle", record.getSubtitle())
                .addValue("type", record.getType())
                .addValue("status", normalizeStatus(record.getStatus()))
                .addValue("owner", record.getOwner())
                .addValue("description", record.getDescription())
                .addValue("imageUrl", record.getImageUrl())
                .addValue("createdAt", record.getCreatedAt() == null ? null : Timestamp.from(record.getCreatedAt()))
                .addValue("updatedAt", record.getUpdatedAt() == null ? null : Timestamp.from(record.getUpdatedAt()));
    }

    private Long findOrCreateFormCategory(String service) {
        String categoryName = isBlank(service) ? "Liên hệ" : service.trim();
        List<Long> ids = jdbcTemplate.query(
                "SELECT form_category_id FROM form_categories WHERE LOWER(category_name) = LOWER(:categoryName) LIMIT 1",
                new MapSqlParameterSource("categoryName", categoryName),
                (rs, rowNum) -> rs.getLong("form_category_id"));
        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                "INSERT INTO form_categories (category_name, created_at) VALUES (:categoryName, NOW(6))",
                new MapSqlParameterSource("categoryName", categoryName),
                keyHolder,
                new String[] { "form_category_id" });
        return keyHolder.getKey().longValue();
    }

    private void applyGeneratedId(CmsRecord record, KeyHolder keyHolder) {
        if (keyHolder.getKey() != null) {
            record.setId(keyHolder.getKey().longValue());
        }
        record.setCreatedAt(Instant.now());
        record.setUpdatedAt(record.getCreatedAt());
        record.setDeleted(false);
    }

    private String normalizeModule(String moduleKey) {
        return isBlank(moduleKey) ? "posts" : moduleKey.trim().toLowerCase();
    }

    private String normalizeStatus(String status) {
        return isBlank(status) ? "ACTIVE" : status.trim().toUpperCase();
    }

    private String slug(String preferred, String fallback) {
        String source = isBlank(preferred) ? fallback : preferred;
        String slug = source == null ? "record" : source.trim().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "record-" + System.currentTimeMillis() : slug + "-" + System.currentTimeMillis();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
