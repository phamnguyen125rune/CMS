package IVS.CMS.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import IVS.CMS.domain.Permission;
import IVS.CMS.repositories.PermissionRepository;
import IVS.CMS.repositories.rowMapper.PermissionRowMapper;

@Repository
public class PermissionRepositoryImpl implements PermissionRepository {
    private static final String PERMISSION_SELECT = """
            SELECT
                p.permission_id,
                COALESCE(api.api_description, CONCAT(api.api_link, ' ', act.action_name)) AS name,
                LOWER(api.api_link) AS resource_code,
                UPPER(act.action_name) AS action,
                CONCAT(LOWER(api.api_link), ':', UPPER(act.action_name)) AS permission_code,
                p.created_at,
                p.created_by,
                p.updated_at,
                p.updated_by
            FROM permissions p
            INNER JOIN actions act ON act.action_id = p.action_id
            INNER JOIN apis api ON api.api_id = p.api_id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PermissionRowMapper mapperDb;

    public PermissionRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate, PermissionRowMapper mapperDb) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapperDb = mapperDb;
    }

    @Override
    public Permission save(Permission permission) {
        permission.normalizePermissionCode();
        long actionId = findOrCreateAction(permission.getAction());
        long apiId = findOrCreateApi(permission.getResourceCode(), permission.getName());

        if (permission.getId() == 0) {
            permission.handleBeforeCreate();
            String sql = """
                    INSERT INTO permissions (
                        action_id,
                        api_id,
                        created_at,
                        created_by,
                        updated_at,
                        updated_by
                    ) VALUES (
                        :actionId,
                        :apiId,
                        :createdAt,
                        :createdBy,
                        :updatedAt,
                        :updatedBy
                    )
                    """;

            KeyHolder keyHolder = new GeneratedKeyHolder();
            MapSqlParameterSource params = mapperDb.toParams(permission)
                    .addValue("actionId", actionId)
                    .addValue("apiId", apiId);
            jdbcTemplate.update(sql, params, keyHolder, new String[] { "permission_id" });

            if (keyHolder.getKey() != null) {
                permission.setId(keyHolder.getKey().longValue());
            }
        } else {
            permission.handleUpdate();
            String sql = """
                    UPDATE permissions
                    SET action_id = :actionId,
                        api_id = :apiId,
                        updated_at = :updatedAt,
                        updated_by = :updatedBy
                    WHERE permission_id = :permissionId
                    """;

            MapSqlParameterSource params = mapperDb.toParams(permission)
                    .addValue("actionId", actionId)
                    .addValue("apiId", apiId);
            jdbcTemplate.update(sql, params);
        }
        return permission;
    }

    @Override
    public Optional<Permission> findById(long id) {
        String sql = PERMISSION_SELECT + "WHERE p.permission_id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        return jdbcTemplate.query(sql, params, mapperDb).stream().findFirst();
    }

    @Override
    public List<Permission> findAll() {
        String sql = PERMISSION_SELECT + "ORDER BY resource_code ASC, action ASC, permission_id ASC";
        return jdbcTemplate.query(sql, mapperDb);
    }

    @Override
    public void delete(Permission permission) {
        String sql = "DELETE FROM permissions WHERE permission_id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", permission.getId());
        jdbcTemplate.update(sql, params);
    }

    @Override
    public List<Permission> findByRoleId(long roleId) {
        String sql = """
                SELECT
                    p.permission_id,
                    COALESCE(api.api_description, CONCAT(api.api_link, ' ', act.action_name)) AS name,
                    LOWER(api.api_link) AS resource_code,
                    UPPER(act.action_name) AS action,
                    CONCAT(LOWER(api.api_link), ':', UPPER(act.action_name)) AS permission_code,
                    p.created_at,
                    p.created_by,
                    p.updated_at,
                    p.updated_by
                FROM permissions p
                INNER JOIN role_permission rp ON p.permission_id = rp.permission_id
                INNER JOIN actions act ON act.action_id = p.action_id
                INNER JOIN apis api ON api.api_id = p.api_id
                WHERE rp.role_id = :roleId
                ORDER BY resource_code ASC, action ASC, p.permission_id ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("roleId", roleId);
        return jdbcTemplate.query(sql, params, mapperDb);
    }

    private long findOrCreateAction(String action) {
        String actionName = action == null || action.isBlank() ? "VIEW" : action.trim().toUpperCase();
        List<Long> ids = jdbcTemplate.query(
                "SELECT action_id FROM actions WHERE UPPER(action_name) = :actionName LIMIT 1",
                new MapSqlParameterSource("actionName", actionName),
                (rs, rowNum) -> rs.getLong("action_id"));
        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                "INSERT INTO actions (action_name) VALUES (:actionName)",
                new MapSqlParameterSource("actionName", actionName),
                keyHolder,
                new String[] { "action_id" });
        return keyHolder.getKey().longValue();
    }

    private long findOrCreateApi(String resourceCode, String description) {
        String apiLink = resourceCode == null || resourceCode.isBlank() ? "general" : resourceCode.trim().toLowerCase();
        List<Long> ids = jdbcTemplate.query(
                "SELECT api_id FROM apis WHERE LOWER(api_link) = LOWER(:apiLink) LIMIT 1",
                new MapSqlParameterSource("apiLink", apiLink),
                (rs, rowNum) -> rs.getLong("api_id"));
        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                "INSERT INTO apis (api_link, api_description) VALUES (:apiLink, :description)",
                new MapSqlParameterSource("apiLink", apiLink).addValue("description", description),
                keyHolder,
                new String[] { "api_id" });
        return keyHolder.getKey().longValue();
    }
}
