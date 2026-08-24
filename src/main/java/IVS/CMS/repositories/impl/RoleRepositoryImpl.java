// package IVS.CMS.repositories.impl;

// import java.util.List;
// import java.util.Optional;

// import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
// import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
// import org.springframework.jdbc.support.GeneratedKeyHolder;
// import org.springframework.jdbc.support.KeyHolder;
// import org.springframework.stereotype.Repository;

// import IVS.CMS.domain.Role;
// import IVS.CMS.repositories.PermissionRepository;
// import IVS.CMS.repositories.RoleRepository;
// import IVS.CMS.repositories.rowMapper.RoleRowMapper;

// @Repository
// public class RoleRepositoryImpl implements RoleRepository {

//     private final NamedParameterJdbcTemplate jdbcTemplate;
//     private final RoleRowMapper mapperDb;
//     private final PermissionRepository permissionRepository;

//     public RoleRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate, RoleRowMapper mapperDb,
//             PermissionRepository permissionRepository) {
//         this.jdbcTemplate = jdbcTemplate;
//         this.mapperDb = mapperDb;
//         this.permissionRepository = permissionRepository;
//     }

//     @Override
//     public Role save(Role role) {
//         if (role.getRoleId() == 0) {
//             String sql = """
//                     INSERT INTO roles (role_name, role_description, is_active, is_system, created_at, created_by)
//                     VALUES (:roleName, :roleDescription, :isActive, :isSystem, :createdAt, :createdBy)
//                     """;
//             KeyHolder keyHolder = new GeneratedKeyHolder();
//             jdbcTemplate.update(sql, mapperDb.toParams(role), keyHolder, new String[] { "role_id" });
//             if (keyHolder.getKey() != null) {
//                 role.setRoleId(keyHolder.getKey().longValue());
//             }
//         } else {
//             String sql = """
//                     UPDATE roles
//                     SET role_name = :roleName,
//                         role_description = :roleDescription,
//                         is_active = :isActive,
//                         is_system = :isSystem,
//                         updated_at = :updatedAt,
//                         updated_by = :updatedBy
//                     WHERE role_id = :roleId
//                     """;
//             jdbcTemplate.update(sql, mapperDb.toParams(role));
//         }
//         return role;
//     }

//     @Override
//     public Optional<Role> findById(long id) {
//         String sql = "SELECT * FROM roles WHERE role_id = :id";
//         MapSqlParameterSource params = new MapSqlParameterSource("id", id);
//         Optional<Role> roleOpt = jdbcTemplate.query(sql, params, mapperDb).stream().findFirst();
//         roleOpt.ifPresent(role -> role.setPermissions(permissionRepository.findByRoleId(role.getRoleId())));
//         return roleOpt;
//     }

//     @Override
//     public List<Role> findAll() {
//         String sql = "SELECT * FROM roles ORDER BY role_id ASC";
//         List<Role> roles = jdbcTemplate.query(sql, mapperDb);
//         roles.forEach(role -> role.setPermissions(permissionRepository.findByRoleId(role.getRoleId())));
//         return roles;
//     }

//     @Override
//     public void delete(Role role) {
//         String deleteMappingSql = "DELETE FROM role_permission WHERE role_id = :roleId";
//         jdbcTemplate.update(deleteMappingSql, new MapSqlParameterSource("roleId", role.getRoleId()));

//         String sql = "DELETE FROM roles WHERE role_id = :id";
//         jdbcTemplate.update(sql, new MapSqlParameterSource("id", role.getRoleId()));
//     }

//     @Override
//     public void updateRolePermissions(long roleId, List<Long> permissionIds) {
//         String deleteSql = "DELETE FROM role_permission WHERE role_id = :roleId";
//         jdbcTemplate.update(deleteSql, new MapSqlParameterSource("roleId", roleId));

//         if (permissionIds != null && !permissionIds.isEmpty()) {
//             String insertSql = "INSERT INTO role_permission (role_id, permission_id) VALUES (:roleId, :permissionId)";
//             MapSqlParameterSource[] batchParams = permissionIds.stream()
//                     .map(permissionId -> new MapSqlParameterSource()
//                             .addValue("roleId", roleId)
//                             .addValue("permissionId", permissionId))
//                     .toArray(MapSqlParameterSource[]::new);
//             jdbcTemplate.batchUpdate(insertSql, batchParams);
//         }
//     }

//     @Override
//     public boolean existsByName(String name) {
//         String sql = "SELECT COUNT(1) FROM roles WHERE LOWER(role_name) = LOWER(:name)";
//         Integer count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("name", name), Integer.class);
//         return count != null && count > 0;
//     }

//     @Override
//     public Optional<Role> findByName(String name) {
//         String sql = "SELECT * FROM roles WHERE LOWER(role_name) = LOWER(:name)";
//         Optional<Role> roleOpt = jdbcTemplate.query(sql, new MapSqlParameterSource("name", name), mapperDb).stream()
//                 .findFirst();
//         roleOpt.ifPresent(role -> role.setPermissions(permissionRepository.findByRoleId(role.getRoleId())));
//         return roleOpt;
//     }
// }