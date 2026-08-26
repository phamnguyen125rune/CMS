package IVS.CMS.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.Action;
import IVS.CMS.domain.Api;
import IVS.CMS.domain.Permission;
import IVS.CMS.domain.Role;
import IVS.CMS.repositories.PermissionRepository;
import IVS.CMS.repositories.rowMapper.ActionRowMapper;
import IVS.CMS.repositories.rowMapper.ApiRowMapper;
import IVS.CMS.repositories.rowMapper.PermissionRowMapper;
import IVS.CMS.services.dto.response.ResActionDTO;

@Repository
public class PermissionRepositoryImpl implements PermissionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ActionRowMapper actionMapperDb;
    private final ApiRowMapper apiMapperDb;
    private final PermissionRowMapper permissionMapperDb;

    public PermissionRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate, ActionRowMapper actionMapperDb,
            ApiRowMapper apiMapperDb, PermissionRowMapper permissionMapperDb) {
        this.jdbcTemplate = jdbcTemplate;
        this.actionMapperDb = actionMapperDb;
        this.apiMapperDb = apiMapperDb;
        this.permissionMapperDb = permissionMapperDb;
    }

    @Override
    public List<ResActionDTO> findAllAction() {
        String sql = "SELECT * FROM actions ORDER BY action_id ASC";
        List<Action> actions = jdbcTemplate.query(sql, actionMapperDb);
        return actions.stream().map(action -> {
            ResActionDTO dto = new ResActionDTO();
            dto.setActionId(action.getActionId());
            dto.setActionName(action.getActionName());

            return dto;
        }).toList();
    }

    @Override
    public List<Api> findAllApi() {
        String sql = "SELECT * FROM apis ORDER BY api_id ASC";
        List<Api> apis = jdbcTemplate.query(sql, apiMapperDb);
        return apis;
    }

    @Override
    public Permission findById(long apiId, long actionId) {

        String sql = """
                SELECT *
                FROM permissions p
                WHERE p.api_id = :apiId
                AND p.action_id = :actionId
                ORDER BY p.permission_id ASC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("apiId", apiId)
                .addValue("actionId", actionId);

        return jdbcTemplate.query(sql, params, permissionMapperDb)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Permission findByLinkApi(String apiLink, String actionName) {

        String sql = """
                SELECT p.* FROM permissions p
                INNER JOIN actions ac ON p.action_id = ac.action_id
                INNER JOIN apis a ON p.api_id = a.api_id
                WHERE a.api_link = :apiLink
                AND ac.action_name = :actionName
                ORDER BY permission_id ASC;
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("apiLink", apiLink)
                .addValue("actionName", actionName);

        return jdbcTemplate.query(sql, params, permissionMapperDb)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional
    public int updateRolePermission(Role role, List<Long> permissionIds) {

        String deleteSql = """
                DELETE FROM role_permission
                WHERE role_id = :roleId
                """;

        MapSqlParameterSource deleteParams = new MapSqlParameterSource()
                .addValue("roleId", role.getRoleId());

        jdbcTemplate.update(deleteSql, deleteParams);

        if (permissionIds == null || permissionIds.isEmpty()) {
            return 0;
        }
        String insertSql = """
                INSERT INTO role_permission (role_id, permission_id)
                VALUES (:roleId, :permissionId)
                """;

        int count = 0;

        for (Long permissionId : permissionIds) {

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("roleId", role.getRoleId())
                    .addValue("permissionId", permissionId);

            count += jdbcTemplate.update(insertSql, params);
        }

        return count;
    }

    @Override
    public Optional<Permission> findById(long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    @Override
    public List<Permission> findByRoleId(long roleId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByRoleId'");
    }
}

// private final PermissionRowMapper mapperDb;

// public PermissionRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate,
// PermissionRowMapper mapperDb) {
// this.jdbcTemplate = jdbcTemplate;
// this.mapperDb = mapperDb;
// }

// @Override
// public Permission save(Permission permission) {
// if (permission.getPermissionId() == null || permission.getPermissionId() ==
// 0) {
// String sql = """
// INSERT INTO permissions (action_id, api_id, created_at, created_by)
// VALUES (:actionId, :apiId, :createdAt, :createdBy)
// """;
// KeyHolder keyHolder = new GeneratedKeyHolder();
// jdbcTemplate.update(sql, mapperDb.toParams(permission), keyHolder, new
// String[] { "permission_id" });
// if (keyHolder.getKey() != null) {
// permission.setPermissionId(keyHolder.getKey().longValue());
// }
// } else {
// String sql = """
// UPDATE permissions
// SET action_id = :actionId,
// api_id = :apiId,
// updated_at = :updatedAt,
// updated_by = :updatedBy
// WHERE permission_id = :permissionId
// """;
// jdbcTemplate.update(sql, mapperDb.toParams(permission));
// }
// return permission;
// }

// @Override
// public Optional<Permission> findById(long id) {
// String sql = """
// SELECT p.*, a.action_name, api.api_link
// FROM permissions p
// INNER JOIN actions a ON p.action_id = a.action_id
// INNER JOIN apis api ON p.api_id = api.api_id
// WHERE p.permission_id = :id
// """;
// return jdbcTemplate.query(sql, new MapSqlParameterSource("id", id),
// mapperDb).stream().findFirst();
// }

// @Override
// public List<Permission> findAll() {
// String sql = """
// SELECT p.*, a.action_name, api.api_link
// FROM permissions p
// INNER JOIN actions a ON p.action_id = a.action_id
// INNER JOIN apis api ON p.api_id = api.api_id
// ORDER BY api.api_link ASC, a.action_name ASC
// """;
// return jdbcTemplate.query(sql, mapperDb);
// }

// @Override
// public void delete(Permission permission) {
// String sql = "DELETE FROM permissions WHERE permission_id = :id";
// jdbcTemplate.update(sql, new MapSqlParameterSource("id",
// permission.getPermissionId()));
// }

// @Override
// public List<Permission> findByRoleId(long roleId) {
// String sql = """
// SELECT p.*, a.action_name, api.api_link
// FROM permissions p
// INNER JOIN role_permission rp ON p.permission_id = rp.permission_id
// INNER JOIN actions a ON p.action_id = a.action_id
// INNER JOIN apis api ON p.api_id = api.api_id
// WHERE rp.role_id = :roleId
// ORDER BY api.api_link ASC, a.action_name ASC
// """;
// return jdbcTemplate.query(sql, new MapSqlParameterSource("roleId", roleId),
// mapperDb);
// }
// }