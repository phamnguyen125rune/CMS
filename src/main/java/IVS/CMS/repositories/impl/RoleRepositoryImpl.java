package IVS.CMS.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.Role;
import IVS.CMS.repositories.PermissionRepository;
import IVS.CMS.repositories.RoleRepository;
import IVS.CMS.repositories.rowMapper.RoleRowMapper;

@Repository
public class RoleRepositoryImpl implements RoleRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RoleRowMapper mapperDb;
    private final PermissionRepository permissionRepository;

    public RoleRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate, RoleRowMapper mapperDb,
            PermissionRepository permissionRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapperDb = mapperDb;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public Role save(Role role) {
        if (role.getId() == 0) {
            role.handleBeforeCreate();
            String sql = "INSERT INTO roles (name, description, active, created_at, created_by, updated_at, updated_by) "
                    + "VALUES (:name, :description, :active, :createdAt, :createdBy, :updatedAt, :updatedBy)";

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(sql, mapperDb.toParams(role), keyHolder, new String[] { "id" });

            if (keyHolder.getKey() != null) {
                role.setId(keyHolder.getKey().longValue());
            }
        } else {
            role.handleUpdate();
            String sql = "UPDATE roles SET name = :name, description = :description, active = :active, "
                    + "updated_at = :updatedAt, updated_by = :updatedBy WHERE id = :id";

            MapSqlParameterSource params = mapperDb.toParams(role);
            params.addValue("id", role.getId());
            jdbcTemplate.update(sql, params);
        }
        return role;
    }

    @Override
    public Optional<Role> findById(long id) {
        String sql = "SELECT * FROM roles WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        Optional<Role> roleOpt = jdbcTemplate.query(sql, params, mapperDb).stream().findFirst();

        roleOpt.ifPresent(role -> {
            role.setPermissions(permissionRepository.findByRoleId(role.getId()));
        });

        return roleOpt;
    }

    @Override
    public List<Role> findAll() {
        String sql = "SELECT * FROM roles";
        List<Role> roles = jdbcTemplate.query(sql, mapperDb);
        roles.forEach(role -> role.setPermissions(permissionRepository.findByRoleId(role.getId())));
        return roles;
    }

    @Override
    @Transactional
    public void delete(Role role) {
        String deleteMappingSql = "DELETE FROM role_permission WHERE role_id = :roleId";
        jdbcTemplate.update(deleteMappingSql, new MapSqlParameterSource("roleId", role.getId()));

        String sql = "DELETE FROM roles WHERE id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", role.getId()));
    }

    @Override
    @Transactional
    public void updateRolePermissions(long roleId, List<Long> permissionIds) {
        String deleteSql = "DELETE FROM role_permission WHERE role_id = :roleId";
        jdbcTemplate.update(deleteSql, new MapSqlParameterSource("roleId", roleId));

        if (permissionIds != null && !permissionIds.isEmpty()) {
            String insertSql = "INSERT INTO role_permission (role_id, permission_id) VALUES (:roleId, :permissionId)";

            MapSqlParameterSource[] batchParams = permissionIds.stream()
                    .map(permissionId -> new MapSqlParameterSource()
                            .addValue("roleId", roleId)
                            .addValue("permissionId", permissionId))
                    .toArray(MapSqlParameterSource[]::new);

            jdbcTemplate.batchUpdate(insertSql, batchParams);
        }

    }

    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(1) FROM roles WHERE name = :name";
        MapSqlParameterSource params = new MapSqlParameterSource("name", name);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public Optional<Role> findByName(String name) {
        String sql = "SELECT * FROM roles WHERE LOWER(name) = LOWER(:name)";
        MapSqlParameterSource params = new MapSqlParameterSource("name", name);
        Optional<Role> roleOpt = jdbcTemplate.query(sql, params, mapperDb).stream().findFirst();

        roleOpt.ifPresent(role -> {
            role.setPermissions(permissionRepository.findByRoleId(role.getId()));
        });

        return roleOpt;
    }
}
