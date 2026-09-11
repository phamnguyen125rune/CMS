package IVS.CMS.repositories.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import IVS.CMS.domain.AuditLog;
import IVS.CMS.domain.dto.request.ReqAuditLogFilterDTO;
import IVS.CMS.domain.dto.response.ResAuditLogDTO;
import IVS.CMS.domain.dto.response.ResAuditLogSearchDTO;
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
            condition.append(" AND (a.action LIKE :keyword OR a.entity_type LIKE :keyword) ");
            params.addValue("keyword", "%" + filter.getKeyword().trim() + "%");
        }

        LocalDateTime from = filter.resolveFrom();
        LocalDateTime to = filter.resolveTo();
        condition.append(" AND a.created_at >= :from AND a.created_at <= :to ");
        params.addValue("from", from);
        params.addValue("to", to);

        if (Boolean.TRUE.equals(filter.getAnonymousOnly())) {
            condition.append(" AND a.user_id IS NULL ");
        } else if (filter.getUserId() != null) {
            condition.append(" AND a.user_id = :userId ");
            params.addValue("userId", filter.getUserId());
        }

        if (filter.getEntityType() != null && !filter.getEntityType().trim().isEmpty()) {
            condition.append(" AND a.entity_type = :entityType ");
            params.addValue("entityType", filter.getEntityType().trim());
        }
        if (filter.getEntityId() != null) {
            condition.append(" AND a.entity_id = :entityId ");
            params.addValue("entityId", filter.getEntityId());
        }

        List<String> actions = filter.resolveActions();
        if (!actions.isEmpty()) {
            condition.append(" AND a.action IN (:actions) ");
            params.addValue("actions", actions);
        }

        if (filter.getStatusCode() != null) {
            condition.append(" AND a.status_code = :statusCode ");
            params.addValue("statusCode", filter.getStatusCode());
        } else {
            if (filter.getMinStatusCode() != null) {
                condition.append(" AND a.status_code >= :minStatusCode ");
                params.addValue("minStatusCode", filter.getMinStatusCode());
            }
            if (filter.getStatusGroup() != null && !filter.getStatusGroup().isBlank()) {
                String sg = filter.getStatusGroup().trim().toUpperCase();
                switch (sg) {
                    case "2XX" -> condition.append(" AND a.status_code >= 200 AND a.status_code < 300 ");
                    case "4XX" -> condition.append(" AND a.status_code >= 400 AND a.status_code < 500 ");
                    case "5XX" -> condition.append(" AND a.status_code >= 500 AND a.status_code < 600 ");
                    case "ERRORS" -> condition.append(" AND a.status_code >= 400 ");
                    default -> {} // All
                }
            }
        }

        return condition.toString();
    }

    private String buildOrderByClause(ReqAuditLogFilterDTO filter) {
        if (filter != null && filter.getSort() != null && !filter.getSort().isBlank()) {
            String s = filter.getSort().trim().toLowerCase();
            if (s.startsWith("createdat,asc") || s.startsWith("created_at,asc")) {
                return " ORDER BY a.created_at ASC, a.log_id ASC ";
            }
        }
        return " ORDER BY a.created_at DESC, a.log_id DESC ";
    }

    @Override
    public List<ResAuditLogDTO> findAll(ReqAuditLogFilterDTO filter, int limit, int offset) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String whereClause = buildFilterCondition(filter, params);
        String orderByClause = buildOrderByClause(filter);

        String sql = """
                SELECT a.log_id, a.user_id, a.entity_type, a.entity_id, a.action,
                       a.old_value, a.new_value, a.created_at, a.status_code
                FROM audit_logs a
                """
                + whereClause + orderByClause + " LIMIT :limit OFFSET :offset";

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

    @Override
    public ResAuditLogSearchDTO.SummaryStats aggregateMetrics(ReqAuditLogFilterDTO filter) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String whereClause = buildFilterCondition(filter, params);

        String sql = """
                SELECT 
                    COUNT(1) AS total_count,
                    COUNT(CASE WHEN a.status_code >= 200 AND a.status_code < 300 THEN 1 END) AS count_2xx,
                    COUNT(CASE WHEN a.status_code >= 400 AND a.status_code < 500 THEN 1 END) AS count_4xx,
                    COUNT(CASE WHEN a.status_code >= 500 AND a.status_code < 600 THEN 1 END) AS count_5xx
                FROM audit_logs a
                """ + whereClause;

        return jdbcTemplate.query(sql, params, rs -> {
            if (rs.next()) {
                long total = rs.getLong("total_count");
                long count2xx = rs.getLong("count_2xx");
                long count4xx = rs.getLong("count_4xx");
                long count5xx = rs.getLong("count_5xx");
                long other = Math.max(0, total - (count2xx + count4xx + count5xx));

                double errorRate = total > 0 ? (((double) (count4xx + count5xx) * 100.0) / total) : 0.0;
                errorRate = Math.round(errorRate * 100.0) / 100.0;

                ResAuditLogSearchDTO.HealthRatio healthRatio = ResAuditLogSearchDTO.HealthRatio.builder()
                        .success2xx(count2xx)
                        .clientError4xx(count4xx)
                        .serverError5xx(count5xx)
                        .other(other)
                        .errorRatePercent(errorRate)
                        .build();

                return ResAuditLogSearchDTO.SummaryStats.builder()
                        .totalRecords(total)
                        .healthRatio(healthRatio)
                        .build();
            }
            return ResAuditLogSearchDTO.SummaryStats.builder()
                    .totalRecords(0)
                    .healthRatio(ResAuditLogSearchDTO.HealthRatio.builder().build())
                    .build();
        });
    }


    @Override
    public List<ResAuditLogDTO> findForExport(ReqAuditLogFilterDTO filter, int limit) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String whereClause = buildFilterCondition(filter, params);
        String orderByClause = buildOrderByClause(filter);

        int safeLimit = Math.min(Math.max(1, limit), 5000);

        String sql = """
                SELECT a.log_id, a.user_id, a.entity_type, a.entity_id, a.action,
                       a.old_value, a.new_value, a.created_at, a.status_code
                FROM audit_logs a
                """
                + whereClause + orderByClause + " LIMIT :limit";

        params.addValue("limit", safeLimit);
        return jdbcTemplate.query(sql, params, auditLogRowMapper);
    }
}
