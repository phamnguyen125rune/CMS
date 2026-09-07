package IVS.CMS.repositories.rowMapper;

import IVS.CMS.domain.FormDetail;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class FormDetailRowMapper implements RowMapper<FormDetail> {

    @Override
    public FormDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
        FormDetail formDetail = new FormDetail();
        formDetail.setFormId(rs.getLong("form_id"));
        formDetail.setFormCode(rs.getString("form_code"));
        formDetail.setFullName(rs.getString("full_name"));
        formDetail.setEmail(rs.getString("email"));
        formDetail.setPhoneNumber(rs.getString("phone_number"));
        formDetail.setCompany(rs.getString("company"));
        formDetail.setFormCategoryId(rs.getLong("form_category_id"));
        formDetail.setMessage(rs.getString("message"));
        formDetail.setStatus(rs.getString("status"));
        formDetail.setReplyMessage(rs.getString("reply_message"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) formDetail.setCreatedAt(createdAt.toLocalDateTime());

        System.out.println("[FormDetailRowMapper] Mapped row " + rowNum + ": form_id=" + formDetail.getFormId() + ", full_name=" + formDetail.getFullName() + ", status=" + formDetail.getStatus());
        
        return formDetail;
    }
}