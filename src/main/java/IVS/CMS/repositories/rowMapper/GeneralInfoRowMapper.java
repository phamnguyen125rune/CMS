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

                info.setGeneralInfoId(
                                rs.getLong("general_info_id"));

                info.setLogo(
                                rs.getString("logo"));

                info.setCompanyName(
                                rs.getString("company_name"));

                info.setWebsiteName(
                                rs.getString("website_name"));

                info.setWebsiteDescription(
                                rs.getString("website_description"));

                info.setEmail(
                                rs.getString("email"));

                info.setFacebookLink(
                                rs.getString("facebook_link"));

                info.setTwitterLink(
                                rs.getString("twitter_link"));

                info.setInstagramLink(
                                rs.getString("instagram_link"));

                info.setLinkedinLink(
                                rs.getString("linkedin_link"));

                info.setYoutubeLink(
                                rs.getString("youtube_link"));

                info.setZaloLink(
                                rs.getString("zalo_link"));

                info.setCompanyPhoneNumber(
                                rs.getString("company_phone_number"));

                info.setAddress(
                                rs.getString("address"));

                info.setWorkingHours(
                                rs.getString("working_hours"));

                info.setMapEmbedUrl(
                                rs.getString("map_embed_url"));

                info.setFooterLinks(
                                rs.getString("footer_links"));

                if (hasColumn(rs, "show_topbar")) {
                    info.setShowTopbar(rs.getObject("show_topbar") != null ? rs.getBoolean("show_topbar") : true);
                }
                if (hasColumn(rs, "topbar_announcement_text")) {
                    info.setTopbarAnnouncementText(rs.getString("topbar_announcement_text"));
                }
                if (hasColumn(rs, "topbar_announcement_url")) {
                    info.setTopbarAnnouncementUrl(rs.getString("topbar_announcement_url"));
                }
                if (hasColumn(rs, "header_cta_text")) {
                    info.setHeaderCtaText(rs.getString("header_cta_text"));
                }
                if (hasColumn(rs, "header_cta_url")) {
                    info.setHeaderCtaUrl(rs.getString("header_cta_url"));
                }
                if (hasColumn(rs, "show_header_search")) {
                    info.setShowHeaderSearch(rs.getObject("show_header_search") != null ? rs.getBoolean("show_header_search") : true);
                }
                if (hasColumn(rs, "show_theme_toggle")) {
                    info.setShowThemeToggle(rs.getObject("show_theme_toggle") != null ? rs.getBoolean("show_theme_toggle") : true);
                }
                if (hasColumn(rs, "show_language_switch")) {
                    info.setShowLanguageSwitch(rs.getObject("show_language_switch") != null ? rs.getBoolean("show_language_switch") : true);
                }
                if (hasColumn(rs, "footer_copyright")) {
                    info.setFooterCopyright(rs.getString("footer_copyright"));
                }
                if (hasColumn(rs, "show_newsletter")) {
                    info.setShowNewsletter(rs.getObject("show_newsletter") != null ? rs.getBoolean("show_newsletter") : true);
                }
                if (hasColumn(rs, "newsletter_title")) {
                    info.setNewsletterTitle(rs.getString("newsletter_title"));
                }
                if (hasColumn(rs, "newsletter_desc")) {
                    info.setNewsletterDesc(rs.getString("newsletter_desc"));
                }
                if (hasColumn(rs, "footer_columns_json")) {
                    info.setFooterColumnsJson(rs.getString("footer_columns_json"));
                }

                Timestamp createdAt = rs.getTimestamp("created_at");

                if (createdAt != null) {
                        info.setCreatedAt(
                                        createdAt.toLocalDateTime());
                }

                info.setCreatedBy(
                                rs.getObject("created_by", Long.class));

                Timestamp updatedAt = rs.getTimestamp("updated_at");

                if (updatedAt != null) {
                        info.setUpdatedAt(
                                        updatedAt.toLocalDateTime());
                }

                info.setUpdatedBy(
                                rs.getObject("updated_by", Long.class));

                return info;
        }

        private boolean hasColumn(ResultSet rs, String columnName) {
                try {
                        rs.findColumn(columnName);
                        return true;
                } catch (SQLException e) {
                        return false;
                }
        }
}