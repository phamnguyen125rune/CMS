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
        if (permission.getId() == 0) {
            permission.handleBeforeCreate();
            String sql = """
                    INSERT INTO permissions (
                        name,
                        resource_code,
                        action,
                        permission_code,
                        created_at,
                        created_by,
                        updated_at,
                        updated_by
                    ) VALUES (
                        :name,
                        :resourceCode,
                        :action,
                        :permissionCode,
                        :createdAt,
                        :createdBy,
                        :updatedAt,
                        :updatedBy
                    )
                    """;

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(sql, mapperDb.toParams(permission), keyHolder, new String[] { "id" });

            if (keyHolder.getKey() != null) {
                permission.setId(keyHolder.getKey().longValue());
            }
        } else {
            permission.handleUpdate();
            String sql = """
                    UPDATE permissions
                    SET name = :name,
                        resource_code = :resourceCode,
                        action = :action,
                        permission_code = :permissionCode,
                        updated_at = :updatedAt,
                        updated_by = :updatedBy
                    WHERE id = :id
                    """;

            MapSqlParameterSource params = mapperDb.toParams(permission);
            params.addValue("id", permission.getId());
            jdbcTemplate.update(sql, params);
        }
        return permission;
    }

    @Override
    public Optional<Permission> findById(long id) {
        String sql = "SELECT * FROM permissions WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        return jdbcTemplate.query(sql, params, mapperDb).stream().findFirst();
    }

    @Override
    public List<Permission> findAll() {
        String sql = "SELECT * FROM permissions ORDER BY resource_code ASC, action ASC, id ASC";
        return jdbcTemplate.query(sql, mapperDb);
    }

    @Override
    public void delete(Permission permission) {
        String sql = "DELETE FROM permissions WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", permission.getId());
        jdbcTemplate.update(sql, params);
    }

    @Override
    public List<Permission> findByRoleId(long roleId) {
        String sql = """
                SELECT p.*
                FROM permissions p
                INNER JOIN role_permission rp ON p.id = rp.permission_id
                WHERE rp.role_id = :roleId
                ORDER BY p.resource_code ASC, p.action ASC, p.id ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("roleId", roleId);
        return jdbcTemplate.query(sql, params, mapperDb);
    }
}
