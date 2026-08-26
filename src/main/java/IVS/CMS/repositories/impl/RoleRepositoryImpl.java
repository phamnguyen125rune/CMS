package IVS.CMS.repositories.impl;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import IVS.CMS.domain.Role;
import IVS.CMS.repositories.RoleRepository;
import IVS.CMS.repositories.rowMapper.RoleRowMapper;
import IVS.CMS.services.dto.response.PermissionLinkDTO;
import IVS.CMS.services.dto.response.ResRoleDTO;

@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RoleRowMapper mapperDb;

    public RoleRepositoryImpl(
            NamedParameterJdbcTemplate jdbcTemplate,
            RoleRowMapper mapperDb) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapperDb = mapperDb;
    }

    @Override
    public Role save(Role role) {

        if (role.getRoleId() == 0) {

            String sql = """
                    INSERT INTO roles (
                        role_name,
                        role_description,
                        is_active,
                        is_system,
                        created_at,
                        created_by
                    )
                    VALUES (
                        :roleName,
                        :roleDescription,
                        :isActive,
                        :isSystem,
                        :createdAt,
                        :createdBy
                    )
                    """;

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(
                    sql,
                    mapperDb.toParams(role),
                    keyHolder,
                    new String[] { "role_id" });

            Number key = keyHolder.getKey();

            if (key != null) {
                role.setRoleId(key.longValue());
            }

        }

        return role;
    }

    @Override
    public List<ResRoleDTO> findAll() {

        String roleSql = """
                SELECT *
                FROM roles
                ORDER BY role_id ASC
                """;

        String permissionSql = """
                SELECT
                    rp.role_id,
                    a.api_link,
                    ac.action_name
                FROM role_permission rp
                JOIN permissions p
                    ON rp.permission_id = p.permission_id
                JOIN apis a
                    ON p.api_id = a.api_id
                JOIN actions ac
                    ON p.action_id = ac.action_id
                ORDER BY rp.role_id ASC, p.permission_id ASC
                """;

        List<Role> roles = jdbcTemplate.query(roleSql, mapperDb);

        List<Map<String, Object>> permissionRows = jdbcTemplate.queryForList(
                permissionSql,
                new MapSqlParameterSource());

        return roles.stream()
                .map(role -> {

                    ResRoleDTO dto = new ResRoleDTO();
                    dto.setRoleId(role.getRoleId());
                    dto.setRoleName(role.getRoleName());
                    dto.setRoleDescription(role.getRoleDescription());
                    dto.setIsActive(role.getIsActive());
                    dto.setIsSystem(role.getIsSystem());

                    List<PermissionLinkDTO> permissions = permissionRows.stream()
                            .filter(row -> ((Number) row.get("role_id"))
                                    .longValue() == role.getRoleId())
                            .map(row -> new PermissionLinkDTO(
                                    (String) row.get("api_link"),
                                    (String) row.get("action_name")))
                            .toList();

                    dto.setPermissions(permissions);

                    return dto;
                })
                .toList();
    }

    @Override
    public Role updateById(Role role) {
        String sql = """
                UPDATE roles
                SET
                    role_name = :roleName,
                    role_description = :roleDescription,
                    is_active = :isActive,
                    is_system = :isSystem,
                    updated_at = :updatedAt,
                    updated_by = :updatedBy
                WHERE role_id = :roleId
                """;

        jdbcTemplate.update(
                sql,
                mapperDb.toParams(role));

        return role;
    }

    @Override
    public Role updateByRoleName(Role role) {
        String sql = """
                UPDATE roles
                SET
                    role_name = :roleName,
                    role_description = :roleDescription,
                    is_active = :isActive,
                    is_system = :isSystem,
                    updated_at = :updatedAt,
                    updated_by = :updatedBy
                WHERE role_name = :roleName
                """;

        jdbcTemplate.update(
                sql,
                mapperDb.toParams(role));

        return role;
    }

    @Override
    public Role findById(Long id) {
        String sql = "SELECT * FROM roles WHERE role_id = :id";
        List<Role> roles = jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), mapperDb);
        return roles.isEmpty() ? null : roles.get(0);
    }

    @Override
    public Role findByRoleName(String roleName) {
        String sql = "SELECT * FROM roles WHERE LOWER(role_name) = LOWER(:roleName)";
        List<Role> roles = jdbcTemplate.query(sql, new MapSqlParameterSource("roleName", roleName), mapperDb);
        return roles.isEmpty() ? null : roles.get(0);
    }

    @Override
    public Role changeRoleStatus(Role role) {

        String sql = """
                UPDATE roles
                SET is_active = NOT is_active,
                    updated_at = CURRENT_TIMESTAMP
                WHERE role_id = :id
                """;

        int rowsAffected = jdbcTemplate.update(
                sql,
                new MapSqlParameterSource("id", role.getRoleId()));

        if (rowsAffected == 0) {
            return null;
        }

        return role;
    }

    @Override
    public void delete(Role role) {
        String sql = "DELETE FROM roles WHERE role_id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", role.getRoleId()));
    }

    @Override
    public Boolean checkIsSystemRole(Long id) {
        String sql = "SELECT is_system FROM roles WHERE role_id = :id";
        Boolean isSystem = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("id", id), Boolean.class);
        return isSystem != null && isSystem;
    }
}

// @Override
// public void delete(Role role) {
// String deleteMappingSql = "DELETE FROM role_permission WHERE role_id =
// :roleId";
// jdbcTemplate.update(deleteMappingSql, new MapSqlParameterSource("roleId",
// role.getRoleId()));

// String sql = "DELETE FROM roles WHERE role_id = :id";
// jdbcTemplate.update(sql, new MapSqlParameterSource("id", role.getRoleId()));
// }

// @Override
// public void updateRolePermissions(long roleId, List<Long> permissionIds) {
// String deleteSql = "DELETE FROM role_permission WHERE role_id = :roleId";
// jdbcTemplate.update(deleteSql, new MapSqlParameterSource("roleId", roleId));

// if (permissionIds != null && !permissionIds.isEmpty()) {
// String insertSql = "INSERT INTO role_permission (role_id, permission_id)
// VALUES (:roleId, :permissionId)";
// MapSqlParameterSource[] batchParams = permissionIds.stream()
// .map(permissionId -> new MapSqlParameterSource()
// .addValue("roleId", roleId)
// .addValue("permissionId", permissionId))
// .toArray(MapSqlParameterSource[]::new);
// jdbcTemplate.batchUpdate(insertSql, batchParams);
// }
// }

// @Override
// public boolean existsByName(String name) {
// String sql = "SELECT COUNT(1) FROM roles WHERE LOWER(role_name) =
// LOWER(:name)";
// Integer count = jdbcTemplate.queryForObject(sql, new
// MapSqlParameterSource("name", name), Integer.class);
// return count != null && count > 0;
// }

// @Override
// public Optional<Role> findByName(String name) {
// String sql = "SELECT * FROM roles WHERE LOWER(role_name) = LOWER(:name)";
// Optional<Role> roleOpt = jdbcTemplate.query(sql, new
// MapSqlParameterSource("name", name), mapperDb).stream()
// .findFirst();
// roleOpt.ifPresent(role ->
// role.setPermissions(permissionRepository.findByRoleId(role.getRoleId())));
// return roleOpt;
// }
// }