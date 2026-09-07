package IVS.CMS.repositories.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.Action;
import IVS.CMS.domain.Permission;
import IVS.CMS.domain.Role;
import IVS.CMS.repositories.PermissionRepository;
import IVS.CMS.repositories.rowMapper.PermissionRowMapper;
import IVS.CMS.services.dto.response.role.ResApiActionDTO;

@Repository
public class PermissionRepositoryImpl implements PermissionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    // private final ActionRowMapper actionMapperDb;
    // private final ApiRowMapper apiMapperDb;
    private final PermissionRowMapper permissionMapperDb;

    public PermissionRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate, 
        // ActionRowMapper actionMapperDb, 
        // ApiRowMapper apiMapperDb, 
        PermissionRowMapper permissionMapperDb){
        this.jdbcTemplate = jdbcTemplate;
        // this.actionMapperDb = actionMapperDb;
        // this.apiMapperDb = apiMapperDb;
        this.permissionMapperDb = permissionMapperDb;
    }

    @Override
    public List<ResApiActionDTO> findAllApiAction() {

        String sql = """
            SELECT 
                ap.api_id,
                ap.api_link,
                ap.api_description,
                a.action_id,
                a.action_name
            FROM apis ap
            INNER JOIN permissions p 
                ON ap.api_id = p.api_id
            INNER JOIN actions a 
                ON p.action_id = a.action_id
            ORDER BY ap.api_id, a.action_id
            """;

        Map<Long, ResApiActionDTO> apiMap = new LinkedHashMap<>();

        jdbcTemplate.query(sql, rs -> {

            Long apiId = rs.getLong("api_id");

            ResApiActionDTO dto = apiMap.get(apiId);

            if (dto == null) {
                dto = new ResApiActionDTO();

                dto.setApiId(apiId);
                dto.setApiLink(rs.getString("api_link"));
                dto.setApiDescription(rs.getString("api_description"));
                dto.setActions(new ArrayList<>());

                apiMap.put(apiId, dto);
            }

            // Tạo Action
            Action action = new Action();
            action.setActionId(rs.getLong("action_id"));
            action.setActionName(rs.getString("action_name"));

            // Thêm Action vào API
            dto.getActions().add(action);
        });

        return new ArrayList<>(apiMap.values());
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
    public int updateRolePermission(Role role, List<Long> permissionIds){

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
