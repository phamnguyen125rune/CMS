package IVS.CMS.repositories.impl;

import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import IVS.CMS.domain.AuditLog;
import IVS.CMS.domain.dto.request.ReqAuditLogFilterDTO;
import IVS.CMS.domain.dto.response.ResAuditLogDTO;
import IVS.CMS.repositories.AuditLogRepository;
import IVS.CMS.repositories.rowMapper.AuditLogRowMapper;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final AuditLogRowMapper auditLogRowMapper;

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

    private String buildFilterCondition(ReqAuditLogFilterDTO filter, MapSqlParameterSource params) {
        StringBuilder condition = new StringBuilder(" WHERE 1=1 ");
        if (filter == null) {
            return condition.toString();
        }

        if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
            condition.append(" AND (a.action LIKE :keyword OR a.entity_type LIKE :keyword OR a.old_value LIKE :keyword OR a.new_value LIKE :keyword) ");
            params.addValue("keyword", "%" + filter.getKeyword().trim() + "%");
        }
        if (filter.getEntityType() != null && !filter.getEntityType().trim().isEmpty()) {
            condition.append(" AND a.entity_type = :entityType ");
            params.addValue("entityType", filter.getEntityType().trim());
        }
        if (filter.getAction() != null && !filter.getAction().trim().isEmpty()) {
            condition.append(" AND a.action = :action ");
            params.addValue("action", filter.getAction().trim());
        }
        if (filter.getUserId() != null) {
            condition.append(" AND a.user_id = :userId ");
            params.addValue("userId", filter.getUserId());
        }
        if (filter.getStatusCode() != null) {
            condition.append(" AND a.status_code = :statusCode ");
            params.addValue("statusCode", filter.getStatusCode());
        }
        if (filter.getFromDate() != null) {
            condition.append(" AND DATE(a.created_at) >= :fromDate ");
            params.addValue("fromDate", filter.getFromDate());
        }
        if (filter.getToDate() != null) {
            condition.append(" AND DATE(a.created_at) <= :toDate ");
            params.addValue("toDate", filter.getToDate());
        }

        return condition.toString();
    }

    @Override
    public List<ResAuditLogDTO> findAll(ReqAuditLogFilterDTO filter, int limit, int offset) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String whereClause = buildFilterCondition(filter, params);

        String sql = """
                SELECT a.log_id, a.user_id, a.entity_type, a.entity_id, a.action,
                       a.old_value, a.new_value, a.created_at, a.status_code,
                       u.full_name AS user_full_name, u.email AS user_email
                FROM audit_logs a
                LEFT JOIN users u ON a.user_id = u.user_id
                """
                + whereClause + " ORDER BY a.created_at DESC, a.log_id DESC LIMIT :limit OFFSET :offset";

        params.addValue("limit", limit).addValue("offset", offset);
        return jdbcTemplate.query(sql, params, auditLogRowMapper);
    }

    @Override
    public long count(ReqAuditLogFilterDTO filter) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String whereClause = buildFilterCondition(filter, params);

        String sql = "SELECT COUNT(1) FROM audit_logs a " + whereClause;
        Long count = jdbcTemplate.queryForObject(sql, params, Long.class);
        return count != null ? count : 0L;
    }
}
