package IVS.CMS.repositories.impl;

import IVS.CMS.domain.Collaborator;
import IVS.CMS.repositories.CollaboratorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CollaboratorRepositoryImpl implements CollaboratorRepository {

        private final NamedParameterJdbcTemplate jdbcTemplate;

        @Override
        public List<Collaborator> getCollaborator() {
                String sql = """
                                SELECT collab_id, collab_name, description, position, company_image, visible,
                                created_at, created_by, updated_at, updated_by
                                FROM collaborator
                                ORDER BY position ASC
                                """;

                return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Collaborator.class));
        }

        @Override
        public Optional<Collaborator> findById(Long id) {
                String sql = """
                                SELECT collab_id, collab_name, description, position, company_image, visible,
                                created_at, created_by, updated_at, updated_by
                                FROM collaborator
                                WHERE collab_id = :collabId
                                """;

                MapSqlParameterSource params = new MapSqlParameterSource("collabId", id);

                List<Collaborator> result = jdbcTemplate.query(
                                sql,
                                params,
                                new BeanPropertyRowMapper<>(Collaborator.class));

                return result.stream().findFirst();
        }

        @Override
        public int save(Collaborator collaborator) {
                String sql = """
                                INSERT INTO collaborator (
                                    collab_name, description, position, company_image, visible,
                                    created_at, created_by
                                )
                                VALUES (
                                    :collabName, :description, :position, :companyImage, :visible,
                                    :createdAt, :createdBy
                                )
                                """;

                MapSqlParameterSource params = new MapSqlParameterSource()
                                .addValue("collabName", collaborator.getCollabName())
                                .addValue("description", collaborator.getDescription())
                                .addValue("position", collaborator.getPosition())
                                .addValue("companyImage", collaborator.getCompanyImage())
                                .addValue("visible", collaborator.getVisible())
                                .addValue("createdAt", collaborator.getCreatedAt())
                                .addValue("createdBy", collaborator.getCreatedBy());

                return jdbcTemplate.update(sql, params);
        }

        @Override
        public int update(Collaborator collaborator) {
                String sql = """
                                UPDATE collaborator
                                SET collab_name = :collabName,
                                    description = :description,
                                    position = :position,
                                    company_image = :companyImage,
                                    visible = :visible,
                                    updated_at = :updatedAt,
                                    updated_by = :updatedBy
                                WHERE collab_id = :collabId
                                """;

                MapSqlParameterSource params = new MapSqlParameterSource()
                                .addValue("collabId", collaborator.getCollabId())
                                .addValue("collabName", collaborator.getCollabName())
                                .addValue("description", collaborator.getDescription())
                                .addValue("position", collaborator.getPosition())
                                .addValue("companyImage", collaborator.getCompanyImage())
                                .addValue("visible", collaborator.getVisible())
                                .addValue("updatedAt", collaborator.getUpdatedAt())
                                .addValue("updatedBy", collaborator.getUpdatedBy());

                return jdbcTemplate.update(sql, params);
        }

        @Override
        public int deleteById(Long id) {
                String sql = """
                                DELETE FROM collaborator
                                WHERE collab_id = :collabId
                                """;

                return jdbcTemplate.update(
                                sql,
                                new MapSqlParameterSource("collabId", id));
        }
}