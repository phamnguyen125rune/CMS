package IVS.CMS.repositories.impl;

import IVS.CMS.domain.GeneralInfo;
import IVS.CMS.repositories.GeneralInfoRepository;
import IVS.CMS.repositories.rowMapper.GeneralInfoRowMapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class GeneralInfoRepositoryImpl
                implements GeneralInfoRepository {

        private final JdbcTemplate jdbcTemplate;
        private final GeneralInfoRowMapper rowMapper;

        public GeneralInfoRepositoryImpl(
                        JdbcTemplate jdbcTemplate,
                        GeneralInfoRowMapper rowMapper) {

                this.jdbcTemplate = jdbcTemplate;
                this.rowMapper = rowMapper;
        }

        @Override
        public Optional<GeneralInfo> findFirst() {

                List<GeneralInfo> list = jdbcTemplate.query(
                                "SELECT * FROM general_info " +
                                                "ORDER BY general_info_id ASC LIMIT 1",
                                rowMapper);

                return list.stream().findFirst();
        }

        @Override
        public GeneralInfo save(GeneralInfo info) {

                String sql = "INSERT INTO general_info " +
                                "(logo, company_name, website_name, website_description, email, " +
                                "facebook_link, twitter_link, instagram_link, linkedin_link, youtube_link, zalo_link, " +
                                "company_phone_number, address, working_hours, map_embed_url, footer_links, " +
                                "show_topbar, topbar_announcement_text, topbar_announcement_url, header_cta_text, header_cta_url, " +
                                "show_header_search, show_theme_toggle, show_language_switch, footer_copyright, show_newsletter, " +
                                "newsletter_title, newsletter_desc, footer_columns_json, created_at, created_by) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                KeyHolder keyHolder = new GeneratedKeyHolder();
                LocalDateTime now = LocalDateTime.now();

                jdbcTemplate.update(connection -> {
                        PreparedStatement ps = connection.prepareStatement(
                                        sql,
                                        Statement.RETURN_GENERATED_KEYS);

                        ps.setString(1, info.getLogo());
                        ps.setString(2, info.getCompanyName());
                        ps.setString(3, info.getWebsiteName());
                        ps.setString(4, info.getWebsiteDescription());
                        ps.setString(5, info.getEmail());
                        ps.setString(6, info.getFacebookLink());
                        ps.setString(7, info.getTwitterLink());
                        ps.setString(8, info.getInstagramLink());
                        ps.setString(9, info.getLinkedinLink());
                        ps.setString(10, info.getYoutubeLink());
                        ps.setString(11, info.getZaloLink());
                        ps.setString(12, info.getCompanyPhoneNumber());
                        ps.setString(13, info.getAddress());
                        ps.setString(14, info.getWorkingHours());
                        ps.setString(15, info.getMapEmbedUrl());
                        ps.setString(16, info.getFooterLinks());

                        ps.setBoolean(17, info.getShowTopbar() != null ? info.getShowTopbar() : true);
                        ps.setString(18, info.getTopbarAnnouncementText());
                        ps.setString(19, info.getTopbarAnnouncementUrl());
                        ps.setString(20, info.getHeaderCtaText());
                        ps.setString(21, info.getHeaderCtaUrl());
                        ps.setBoolean(22, info.getShowHeaderSearch() != null ? info.getShowHeaderSearch() : true);
                        ps.setBoolean(23, info.getShowThemeToggle() != null ? info.getShowThemeToggle() : true);
                        ps.setBoolean(24, info.getShowLanguageSwitch() != null ? info.getShowLanguageSwitch() : true);
                        ps.setString(25, info.getFooterCopyright());
                        ps.setBoolean(26, info.getShowNewsletter() != null ? info.getShowNewsletter() : true);
                        ps.setString(27, info.getNewsletterTitle());
                        ps.setString(28, info.getNewsletterDesc());
                        ps.setString(29, info.getFooterColumnsJson());

                        ps.setTimestamp(30, Timestamp.valueOf(now));
                        ps.setObject(31, info.getCreatedBy());

                        return ps;
                }, keyHolder);

                if (keyHolder.getKey() != null) {
                        info.setGeneralInfoId(
                                        keyHolder.getKey().longValue());
                }

                info.setCreatedAt(now);
                return info;
        }

        @Override
        public GeneralInfo update(GeneralInfo info) {

                String sql = "UPDATE general_info SET " +
                                "logo = ?, company_name = ?, website_name = ?, website_description = ?, email = ?, " +
                                "facebook_link = ?, twitter_link = ?, instagram_link = ?, linkedin_link = ?, youtube_link = ?, zalo_link = ?, " +
                                "company_phone_number = ?, address = ?, working_hours = ?, map_embed_url = ?, footer_links = ?, " +
                                "show_topbar = ?, topbar_announcement_text = ?, topbar_announcement_url = ?, header_cta_text = ?, header_cta_url = ?, " +
                                "show_header_search = ?, show_theme_toggle = ?, show_language_switch = ?, footer_copyright = ?, show_newsletter = ?, " +
                                "newsletter_title = ?, newsletter_desc = ?, footer_columns_json = ?, updated_at = ?, updated_by = ? " +
                                "WHERE general_info_id = ?";

                LocalDateTime now = LocalDateTime.now();

                jdbcTemplate.update(
                                sql,
                                info.getLogo(),
                                info.getCompanyName(),
                                info.getWebsiteName(),
                                info.getWebsiteDescription(),
                                info.getEmail(),

                                info.getFacebookLink(),
                                info.getTwitterLink(),
                                info.getInstagramLink(),
                                info.getLinkedinLink(),
                                info.getYoutubeLink(),
                                info.getZaloLink(),

                                info.getCompanyPhoneNumber(),
                                info.getAddress(),
                                info.getWorkingHours(),
                                info.getMapEmbedUrl(),
                                info.getFooterLinks(),

                                info.getShowTopbar() != null ? info.getShowTopbar() : true,
                                info.getTopbarAnnouncementText(),
                                info.getTopbarAnnouncementUrl(),
                                info.getHeaderCtaText(),
                                info.getHeaderCtaUrl(),
                                info.getShowHeaderSearch() != null ? info.getShowHeaderSearch() : true,
                                info.getShowThemeToggle() != null ? info.getShowThemeToggle() : true,
                                info.getShowLanguageSwitch() != null ? info.getShowLanguageSwitch() : true,
                                info.getFooterCopyright(),
                                info.getShowNewsletter() != null ? info.getShowNewsletter() : true,
                                info.getNewsletterTitle(),
                                info.getNewsletterDesc(),
                                info.getFooterColumnsJson(),

                                Timestamp.valueOf(now),
                                info.getUpdatedBy(),
                                info.getGeneralInfoId());

                info.setUpdatedAt(now);
                return info;
        }
}