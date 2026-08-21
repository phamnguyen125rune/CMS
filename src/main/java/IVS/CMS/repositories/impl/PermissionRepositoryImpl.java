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

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PermissionRowMapper mapperDb;

    public PermissionRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate, PermissionRowMapper mapperDb) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapperDb = mapperDb;
    }

    @Override
    public Permission save(Permission permission) {
        if (permission.getPermissionId() == null || permission.getPermissionId() == 0) {
            String sql = """
                    INSERT INTO permissions (action_id, api_id, created_at, created_by)
                    VALUES (:actionId, :apiId, :createdAt, :createdBy)
                    """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(sql, mapperDb.toParams(permission), keyHolder, new String[] { "permission_id" });
            if (keyHolder.getKey() != null) {
                permission.setPermissionId(keyHolder.getKey().longValue());
            }
        } else {
            String sql = """
                    UPDATE permissions
                    SET action_id = :actionId,
                        api_id = :apiId,
                        updated_at = :updatedAt,
                        updated_by = :updatedBy
                    WHERE permission_id = :permissionId
                    """;
            jdbcTemplate.update(sql, mapperDb.toParams(permission));
        }
        return permission;
    }

    @Override
    public Optional<Permission> findById(long id) {
        String sql = """
                SELECT p.*, a.action_name, api.api_link
                FROM permissions p
                INNER JOIN actions a ON p.action_id = a.action_id
                INNER JOIN apis api ON p.api_id = api.api_id
                WHERE p.permission_id = :id
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), mapperDb).stream().findFirst();
    }

    @Override
    public List<Permission> findAll() {
        String sql = """
                SELECT p.*, a.action_name, api.api_link
                FROM permissions p
                INNER JOIN actions a ON p.action_id = a.action_id
                INNER JOIN apis api ON p.api_id = api.api_id
                ORDER BY api.api_link ASC, a.action_name ASC
                """;
        return jdbcTemplate.query(sql, mapperDb);
    }

    @Override
    public void delete(Permission permission) {
        String sql = "DELETE FROM permissions WHERE permission_id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", permission.getPermissionId()));
    }

    @Override
    public List<Permission> findByRoleId(long roleId) {
        String sql = """
                SELECT p.*, a.action_name, api.api_link
                FROM permissions p
                INNER JOIN role_permission rp ON p.permission_id = rp.permission_id
                INNER JOIN actions a ON p.action_id = a.action_id
                INNER JOIN apis api ON p.api_id = api.api_id
                WHERE rp.role_id = :roleId
                ORDER BY api.api_link ASC, a.action_name ASC
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("roleId", roleId), mapperDb);
    }
}