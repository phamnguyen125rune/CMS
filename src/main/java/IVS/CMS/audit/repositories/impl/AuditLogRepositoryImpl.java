package IVS.CMS.audit.repositories.impl;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import IVS.CMS.audit.repositories.AuditLogRepository;
import IVS.CMS.domain.AuditLog;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void save(AuditLog log) {
        String sql = """
                INSERT INTO audit_logs (
                    user_id, entity_type, entity_id, action,
                    old_value, new_value, created_at, status_code
                ) VALUES (
                    :userId, :entityType, :entityId, :action,
                    :oldValue, :newValue, :createdAt, :statusCode
                )
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", log.getUserId())
                .addValue("entityType", log.getEntityType())
                .addValue("entityId", log.getEntityId())
                .addValue("action", log.getAction())
                .addValue("oldValue", log.getOldValue())
                .addValue("newValue", log.getNewValue())
                .addValue("createdAt", log.getCreatedAt())
                .addValue("statusCode", log.getStatusCode());

        jdbcTemplate.update(sql, params);
    }
}
