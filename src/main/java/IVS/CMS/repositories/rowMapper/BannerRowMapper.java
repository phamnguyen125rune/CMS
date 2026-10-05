package IVS.CMS.repositories.rowMapper;

import IVS.CMS.domain.Banner;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class BannerRowMapper implements RowMapper<Banner> {

    @Override
    public Banner mapRow(ResultSet rs, int rowNum) throws SQLException {
        Banner banner = new Banner();
        banner.setBannerId(rs.getLong("banner_id"));
        banner.setTitle(rs.getString("title"));
        banner.setHighlightText(rs.getString("highlight_text"));
        banner.setSubtitle(rs.getString("subtitle"));
        banner.setDescription(rs.getString("description"));
        banner.setImageUrl(rs.getString("image_url"));
        banner.setMobileImageUrl(rs.getString("mobile_image_url"));
        banner.setPrimaryBtnText(rs.getString("primary_btn_text"));
        banner.setPrimaryBtnUrl(rs.getString("primary_btn_url"));
        banner.setSecondaryBtnText(rs.getString("secondary_btn_text"));
        banner.setSecondaryBtnUrl(rs.getString("secondary_btn_url"));
        banner.setStatsJson(rs.getString("stats_json"));
        banner.setFloatingBadgeText(rs.getString("floating_badge_text"));
        banner.setPosition(rs.getString("position"));
        banner.setDisplayOrder(rs.getInt("display_order"));
        banner.setIsActive(rs.getBoolean("is_active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            banner.setCreatedAt(createdAt.toLocalDateTime());
        }
        banner.setCreatedBy(rs.getObject("created_by", Long.class));

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            banner.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        banner.setUpdatedBy(rs.getObject("updated_by", Long.class));

        return banner;
    }
}
