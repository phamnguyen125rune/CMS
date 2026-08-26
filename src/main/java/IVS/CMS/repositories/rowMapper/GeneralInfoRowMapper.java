package IVS.CMS.repositories.rowMapper;

import IVS.CMS.domain.GeneralInfo;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class GeneralInfoRowMapper implements RowMapper<GeneralInfo> {
    @Override
    public GeneralInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
        GeneralInfo info = new GeneralInfo();
        info.setGeneralInfoId(rs.getLong("general_info_id"));
        info.setLogo(rs.getString("logo"));
        info.setCompanyName(rs.getString("company_name"));
        info.setWebsiteName(rs.getString("website_name"));
        info.setWebsiteDescription(rs.getString("website_description"));
        info.setEmail(rs.getString("email"));
        info.setFacebookLink(rs.getString("facebook_link"));
        info.setTwitterLink(rs.getString("twitter_link"));
        info.setInstagramLink(rs.getString("instagram_link"));
        info.setLinkedinLink(rs.getString("linkedin_link"));
        info.setYoutubeLink(rs.getString("youtube_link"));
        info.setZaloLink(rs.getString("zalo_link"));
        info.setCompanyPhoneNumber(rs.getString("company_phone_number"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) info.setCreatedAt(createdAt.toLocalDateTime());
        info.setCreatedBy(rs.getObject("created_by", Long.class));

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) info.setUpdatedAt(updatedAt.toLocalDateTime());
        info.setUpdatedBy(rs.getObject("updated_by", Long.class));

        return info;
    }
}