package IVS.CMS.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service("permissionService")
@RequiredArgsConstructor
public class PermissionService {

    private final JdbcTemplate jdbcTemplate;

    public boolean hasPermission(String api, String action) {

        Long userId = SecurityService.getCurrentUserId()
                .orElse(null);

        if (userId == null) {
            return false;
        }

        String sql = """
            SELECT COUNT(*)
            FROM users u
            JOIN role_permission rp
                ON u.role_id = rp.role_id
            JOIN permissions p
                ON rp.permission_id = p.permission_id
            JOIN actions a
                ON p.action_id = a.action_id
            JOIN apis api
                ON p.api_id = api.api_id
            WHERE u.user_id = ?
              AND u.is_active = TRUE
              AND api.api_link = ?
              AND a.action_name = ?
            """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                userId,
                api,
                action
        );

        return count != null && count > 0;
    }
}
