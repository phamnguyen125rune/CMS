package IVS.CMS.repositories.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import IVS.CMS.domain.Role;
import IVS.CMS.domain.User;
import IVS.CMS.repositories.RoleRepository;
import IVS.CMS.repositories.rowMapper.RoleRowMapper;
import IVS.CMS.repositories.rowMapper.UserRowMapper;
import IVS.CMS.security.SecurityService;
import IVS.CMS.services.dto.response.role.PermissionLinkDTO;
import IVS.CMS.services.dto.response.role.ResRoleDTO;

@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RoleRowMapper mapperDb;
    private final UserRowMapper userRowMapper;

    public RoleRepositoryImpl(
            NamedParameterJdbcTemplate jdbcTemplate,
            RoleRowMapper mapperDb,
            UserRowMapper userRowMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapperDb = mapperDb;
        this.userRowMapper = userRowMapper;
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
                    new String[]{"role_id"}
            );

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
            SELECT
                r.role_id,
                r.role_name,
                r.role_description,
                r.is_active,
                r.is_system,
                COUNT(u.user_id) AS member_count
            FROM roles r
            LEFT JOIN users u
                ON r.role_id = u.role_id
            GROUP BY
                r.role_id,
                r.role_name,
                r.role_description,
                r.is_active,
                r.is_system
            ORDER BY r.role_id ASC
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

        // Query roles + member count
        List<Map<String, Object>> roleRows =
                jdbcTemplate.queryForList(roleSql,
                    new MapSqlParameterSource());

        // Query permissions
        List<Map<String, Object>> permissionRows =
                jdbcTemplate.queryForList(permissionSql,
                        new MapSqlParameterSource());

        // Group permissions theo role_id
        Map<Long, List<PermissionLinkDTO>> permissionsByRole =
                permissionRows.stream()
                        .collect(Collectors.groupingBy(
                                row -> ((Number) row.get("role_id")).longValue(),
                                Collectors.mapping(
                                        row -> new PermissionLinkDTO(
                                                (String) row.get("api_link"),
                                                (String) row.get("action_name")
                                        ),
                                        Collectors.toList()
                                )
                        ));

        return roleRows.stream()
                .map(row -> {

                    long roleId = ((Number) row.get("role_id")).longValue();

                    ResRoleDTO dto = new ResRoleDTO();

                    dto.setRoleId(roleId);
                    dto.setRoleName((String) row.get("role_name"));
                    dto.setRoleDescription((String) row.get("role_description"));
                    dto.setIsActive((Boolean) row.get("is_active"));
                    dto.setIsSystem((Boolean) row.get("is_system"));

                    dto.setMemmberCount(
                            ((Number) row.get("member_count")).longValue()
                    );

                    dto.setPermissions(
                            permissionsByRole.getOrDefault(
                                    roleId,
                                    Collections.emptyList()
                            )
                    );

                    return dto;
                })
                .toList();
    }

    @Override
    public List<User> getUsersByRoleId(Long roleId) {
        String sql = """
            SELECT *
            FROM users
            WHERE role_id = :roleId
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("roleId", roleId);

        return jdbcTemplate.query(sql, params, userRowMapper);
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
                mapperDb.toParams(role)
        );

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
                mapperDb.toParams(role)
        );

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
                new MapSqlParameterSource("id", role.getRoleId())
        );

        if (rowsAffected == 0) {
            return null;
        }

        return role;
    }

    @Override 
    public List<User> searchUsersNotInRole(Long roleId, String keyword) {
        String sql = """
                SELECT *
                FROM users
                WHERE role_id <> :roleId
                  AND user_id <> :currentUserId
                  AND
                   (
                        employee_code LIKE :keyword
                        OR full_name LIKE :keyword
                        OR email LIKE :keyword
                  )
                ORDER BY full_name ASC
                """;
        
        String searchKeyword = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("roleId", roleId)
                .addValue("keyword", searchKeyword)
                .addValue("currentUserId", SecurityService.getCurrentUserId().orElse(null));
        return jdbcTemplate.query(sql, params, userRowMapper);
    }

    @Override
    public int updateUsersRole(List<Long> userIds, Long roleId) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }

        String sql = """
                UPDATE users
                SET
                    role_id = :roleId,
                    updated_at = CURRENT_TIMESTAMP,
                    updated_by = :currentUserId
                WHERE user_id IN (:userIds)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("roleId", roleId)
                .addValue("userIds", userIds)
                .addValue("currentUserId", SecurityService.getCurrentUserId().orElse(null));

        return jdbcTemplate.update(sql, params);
    }

    @Override 
    public int setUsersToDefaultRole(List<Long> userIds){
                if (userIds == null || userIds.isEmpty()) {
            return 0;
        }

        String sql = """
                UPDATE users
                SET
                    role_id = (
                        SELECT role_id
                        FROM roles
                        WHERE role_name = 'DEFAULT_ROLE'
                        LIMIT 1
                    ),
                    updated_at = CURRENT_TIMESTAMP,
                    updated_by = :currentUserId
                WHERE user_id IN (:userIds)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userIds", userIds)
                .addValue("currentUserId", SecurityService.getCurrentUserId().orElse(null));

        return jdbcTemplate.update(sql, params);
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