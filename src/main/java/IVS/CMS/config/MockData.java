package IVS.CMS.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static io.micrometer.common.util.StringUtils.isBlank;

@Component
public class MockData implements CommandLineRunner{
    private static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    private static final String ROLE_NORMAL_USER = "NORMAL_USER";
    private final PasswordEncoder passwordEncoder;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    
    @Value("${CMS.default-admin.email:}")
    private String defaultAdminEmail;
    @Value("${CMS.default-admin.password:}")
    private String defaultAdminPassword;
    @Value("${CMS.default-admin.phone:0900000000}")
    private String defaultAdminPhone;

    public MockData( PasswordEncoder passwordEncoder, NamedParameterJdbcTemplate jdbcTemplate ) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        insertRoles();

        insertActions();

        insertApis();

        insertSuperAdminPermissions();

        insertNormalUserPermissions();

        assignPermissionsToRoles();

        insertDefaultAdminUser();
    }

    private void insertRoles() {
        insertRole(ROLE_SUPER_ADMIN, "Quản trị hệ thống, có toàn quyền");
        insertRole(ROLE_NORMAL_USER, "Người dùng mặc định, chưa thuộc nhóm quyền nghiệp vụ");
    }

    private void insertRole(String roleName, String description) {

        jdbcTemplate.update("""
            INSERT INTO roles
            (
                role_name,
                description,
                isSystem,
                status,
                created_at,
                created_by,
                last_updated_at,
                last_updated_by
            )
            SELECT
                :roleName,
                :description,
                TRUE,
                TRUE,
                NOW(),
                0,
                NOW(),
                0
            WHERE NOT EXISTS (
                SELECT 1
                FROM roles
                WHERE role_name = :roleName
            )
        """,
        new MapSqlParameterSource()
            .addValue("roleName", roleName)
            .addValue("description", description));
    }

    // private void insertPermissions() {
    //     String[][] perms = {
    //             { "Đổi mật khẩu", "auth", "EDIT" }, { "Xem hồ sơ cá nhân", "profile", "VIEW" },
    //             { "Cập nhật hồ sơ cá nhân", "profile", "EDIT" }, { "Xem người dùng", "users", "VIEW" },
    //             { "Quản lý người dùng", "users", "EDIT" }, { "Xem nhóm quyền", "roles", "VIEW" },
    //             { "Quản lý nhóm quyền", "roles", "EDIT" }, { "Xem quyền", "permissions", "VIEW" },
    //             { "Quản lý quyền", "permissions", "EDIT" }, { "Xem chức năng A", "screen_a", "VIEW" },
    //             { "Sửa chức năng A", "screen_a", "EDIT" }, { "Xem chức năng B", "screen_b", "VIEW" },
    //             { "Sửa chức năng B", "screen_b", "EDIT" }, { "Xem chức năng C", "screen_c", "VIEW" },
    //             { "Sửa chức năng C", "screen_c", "EDIT" },
    //             { "Xem liên hệ", "contacts", "VIEW" },
    //             { "Quản lý & Phản hồi liên hệ", "contacts", "EDIT" }
    //     };
    //     for (String[] p : perms)
    //         insertPermission(p[0], p[1], p[2]);
    // }

    private void insertApis() {

        insertApi("/api/profile");
        insertApi("/api/screen-a");
        insertApi("/api/screen-b");
        insertApi("/api/screen-c");

        // Các API khác của hệ thống thêm ở đây
        insertApi("/api/users");
        insertApi("/api/roles");
        insertApi("/api/permissions");
        insertApi("/api/contacts");
    }

    private void insertApi(String apiLink) {

    jdbcTemplate.update(
        """
        INSERT INTO api (api_link)
        SELECT :apiLink
        WHERE NOT EXISTS (
            SELECT 1
            FROM api
            WHERE api_link = :apiLink
        )
        """,
        new MapSqlParameterSource()
            .addValue("apiLink", apiLink)
    );
}


    private void insertActions() {

        insertAction("CREATE");
        insertAction("UPDATE");
        insertAction("VIEW");
        insertAction("DELETE");
    }

    private void insertAction(String actionName) {

        jdbcTemplate.update(
            """
            INSERT INTO actions (action_name)
            SELECT :actionName
            WHERE NOT EXISTS (
                SELECT 1
                FROM actions
                WHERE action_name = :actionName
            )
            """,
            new MapSqlParameterSource()
                .addValue("actionName", actionName)
        );
    }


    private int getActionId(String actionName) {

        return jdbcTemplate.queryForObject(
            """
            SELECT action_id
            FROM actions
            WHERE action_name = :actionName
            """,
            new MapSqlParameterSource()
                .addValue("actionName", actionName),
            Integer.class
        );
    }


    private int getRoleId(String roleName) {

        return jdbcTemplate.queryForObject(
            """
            SELECT role_id
            FROM roles
            WHERE role_name = :roleName
            """,
            new MapSqlParameterSource()
                .addValue("roleName", roleName),
            Integer.class
        );
    }


    private int getApiId(String apiLink) {

        return jdbcTemplate.queryForObject(
            """
            SELECT id
            FROM api
            WHERE api_link = :apiLink
            """,
            new MapSqlParameterSource()
                .addValue("apiLink", apiLink),
            Integer.class
        );
    }

    private void insertSuperAdminPermissions() {

        int roleId = getRoleId(ROLE_SUPER_ADMIN);

        List<Integer> actionIds = jdbcTemplate.query(
            """
            SELECT action_id
            FROM actions
            """,
            new MapSqlParameterSource(),
            (rs, rowNum) -> rs.getInt("action_id")
        );

        List<Integer> apiIds = jdbcTemplate.query(
            """
            SELECT id
            FROM api
            """,
            new MapSqlParameterSource(),
            (rs, rowNum) -> rs.getInt("id")
        );

        for (Integer actionId : actionIds) {
            for (Integer apiId : apiIds) {
                insertPermission(
                    roleId,
                    actionId,
                    apiId
                );
            }
        }
    }

    private void insertNormalUserPermissions() {

        int roleId = getRoleId(ROLE_NORMAL_USER);

        int viewActionId = getActionId("VIEW");
        int updateActionId = getActionId("UPDATE");

        int profileApiId = getApiId("/api/profile");
        int screenAApiId = getApiId("/api/screen-a");
        int screenBApiId = getApiId("/api/screen-b");
        int screenCApiId = getApiId("/api/screen-c");

        // PROFILE
        insertPermission(
            roleId,
            viewActionId,
            profileApiId
        );

        insertPermission(
            roleId,
            updateActionId,
            profileApiId
        );

        // SCREEN A
        insertPermission(
            roleId,
            viewActionId,
            screenAApiId
        );

        // SCREEN B
        insertPermission(
            roleId,
            viewActionId,
            screenBApiId
        );

        // SCREEN C
        insertPermission(
            roleId,
            viewActionId,
            screenCApiId
        );
    }


    private void insertPermission(int roleId, int actionId, int apiId) {

        jdbcTemplate.update(
            """
            INSERT INTO permissions (
                role_id,
                action_id,
                api_id
            )
            SELECT
                :roleId,
                :actionId,
                :apiId
            WHERE NOT EXISTS (
                SELECT 1
                FROM permissions
                WHERE role_id = :roleId
                AND action_id = :actionId
                AND api_id = :apiId
            )
            """,
            new MapSqlParameterSource()
                .addValue("roleId", roleId)
                .addValue("actionId", actionId)
                .addValue("apiId", apiId)
        );
    }

    private void assignPermissionsToRoles() {

        int superAdminRoleId = getRoleId(ROLE_SUPER_ADMIN);
        int normalUserRoleId = getRoleId(ROLE_NORMAL_USER);

        /*
        * ==========================================
        * SUPER ADMIN
        * ==========================================
        */

        jdbcTemplate.update(
            """
            INSERT INTO role_permission (
                role_id,
                permission_id
            )
            SELECT
                p.role_id,
                p.permission_id
            FROM permissions p
            WHERE p.role_id = :roleId
            AND NOT EXISTS (
                SELECT 1
                FROM role_permission rp
                WHERE rp.role_id = p.role_id
                    AND rp.permission_id = p.permission_id
            )
            """,
            new MapSqlParameterSource()
                .addValue("roleId", superAdminRoleId)
        );

        /*
        * ==========================================
        * NORMAL USER
        * ==========================================
        */

        // Xóa permission cũ
        jdbcTemplate.update(
            """
            DELETE FROM role_permission
            WHERE role_id = :roleId
            """,
            new MapSqlParameterSource()
                .addValue("roleId", normalUserRoleId)
        );

        int viewActionId = getActionId("VIEW");
        int updateActionId = getActionId("UPDATE");

        int profileApiId = getApiId("/api/profile");
        int screenAApiId = getApiId("/api/screen-a");
        int screenBApiId = getApiId("/api/screen-b");
        int screenCApiId = getApiId("/api/screen-c");

        insertRolePermission(
            normalUserRoleId,
            viewActionId,
            profileApiId
        );

        insertRolePermission(
            normalUserRoleId,
            updateActionId,
            profileApiId
        );

        insertRolePermission(
            normalUserRoleId,
            viewActionId,
            screenAApiId
        );

        insertRolePermission(
            normalUserRoleId,
            viewActionId,
            screenBApiId
        );

        insertRolePermission(
            normalUserRoleId,
            viewActionId,
            screenCApiId
        );
    }

    private void insertDefaultAdminUser() {
        Long roleId = findRoleIdByName(ROLE_SUPER_ADMIN);

        if (roleId == null || isBlank(defaultAdminEmail) || isBlank(defaultAdminPassword)) {
            return;
        }

        jdbcTemplate.update(
            """
            INSERT INTO users (
                username,
                full_name,
                password_hash,
                phone_number,
                gender,
                isDeleted,
                role_id,
                isSystem,
                created_at
            )
            SELECT
                :username,
                'Admin System',
                :pwd,
                :phone,
                'others',
                FALSE,
                :roleId,
                TRUE,
                NOW()
            WHERE NOT EXISTS (
                SELECT 1
                FROM users
                WHERE username = :username
            )
            """,
            new MapSqlParameterSource()
                .addValue("username", defaultAdminEmail.trim())
                .addValue("pwd", passwordEncoder.encode(defaultAdminPassword))
                .addValue("phone", isBlank(defaultAdminPhone)
                        ? null
                        : defaultAdminPhone.trim())
                .addValue("roleId", roleId)
        );
    }

    private Long findRoleIdByName(String roleName) {
        List<Long> ids = jdbcTemplate.query(
            "SELECT role_id FROM roles WHERE role_name = :name LIMIT 1",
            new MapSqlParameterSource("name", roleName),
            (rs, rowNum) -> rs.getLong("role_id")
        );

        return ids.isEmpty() ? null : ids.get(0);
    }

    private void insertRolePermission(int roleId,int actionId,int apiId) {

            jdbcTemplate.update(
                """
                INSERT INTO role_permission (
                    role_id,
                    permission_id
                )
                SELECT
                    :roleId,
                    p.permission_id
                FROM permissions p
                WHERE p.role_id = :roleId
                AND p.action_id = :actionId
                AND p.api_id = :apiId
                AND NOT EXISTS (
                    SELECT 1
                    FROM role_permission rp
                    WHERE rp.role_id = :roleId
                        AND rp.permission_id = p.permission_id
                )
                """,
                new MapSqlParameterSource()
                    .addValue("roleId", roleId)
                    .addValue("actionId", actionId)
                    .addValue("apiId", apiId)
            );
    }
}
