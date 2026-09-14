package IVS.CMS.repositories.impl;

import IVS.CMS.domain.CollaboratorSetting;
import IVS.CMS.repositories.CollaboratorSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CollaboratorSettingRepositoryImpl implements CollaboratorSettingRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<CollaboratorSetting> find() {
        String sql = """
                SELECT setting_id, columns_per_row, updated_at, updated_by
                FROM collaborator_settings
                LIMIT 1
                """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource(), (rs, rowNum) -> {
            CollaboratorSetting setting = new CollaboratorSetting();
            setting.setSettingId(rs.getLong("setting_id"));
            setting.setColumnsPerRow(rs.getInt("columns_per_row"));
            setting.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

            long updatedBy = rs.getLong("updated_by");
            setting.setUpdatedBy(rs.wasNull() ? null : updatedBy);

            return setting;
        }).stream().findFirst();
    }

    @Override
    public CollaboratorSetting update(Integer columnsPerRow, Long updatedBy) {
        String sql = """
                UPDATE collaborator_settings
                SET columns_per_row = :columnsPerRow,
                    updated_by = :updatedBy
                WHERE setting_id = 1
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("columnsPerRow", columnsPerRow)
                .addValue("updatedBy", updatedBy);

        jdbcTemplate.update(sql, params);

        return find().orElseThrow(() ->
                new RuntimeException("Không tìm thấy cấu hình collaborator"));
    }
}