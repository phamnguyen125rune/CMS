package IVS.CMS.repositories.rowMapper;

import IVS.CMS.domain.Collaborator;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CollaboratorRowMapper implements RowMapper<Collaborator> {

    @Override
    public Collaborator mapRow(ResultSet rs, int rowNum) throws SQLException {
        Collaborator collaborator = new Collaborator();

        collaborator.setCollabId(rs.getLong("collab_id"));
        collaborator.setCollabName(rs.getString("collab_name"));
        collaborator.setPosition(rs.getInt("position"));
        collaborator.setCompanyImage(rs.getString("company_image"));
        collaborator.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        long createdBy = rs.getLong("created_by");
        collaborator.setCreatedBy(rs.wasNull() ? null : createdBy);

        if (rs.getTimestamp("updated_at") != null) {
            collaborator.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }

        long updatedBy = rs.getLong("updated_by");
        collaborator.setUpdatedBy(rs.wasNull() ? null : updatedBy);

        return collaborator;
    }
}