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
public class GeneralInfoRepositoryImpl implements GeneralInfoRepository {

    private final JdbcTemplate jdbcTemplate;
    private final GeneralInfoRowMapper rowMapper;

    public GeneralInfoRepositoryImpl(JdbcTemplate jdbcTemplate, GeneralInfoRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    @Override
    public Optional<GeneralInfo> findFirst() {
        List<GeneralInfo> list = jdbcTemplate.query("SELECT * FROM general_info ORDER BY general_info_id ASC LIMIT 1", rowMapper);
        return list.stream().findFirst();
    }

    @Override
    public GeneralInfo save(GeneralInfo info) {
        String sql = "INSERT INTO general_info (logo, company_name, website_name, website_description, email, " +
                     "facebook_link, twitter_link, instagram_link, linkedin_link, youtube_link, zalo_link, company_phone_number, created_at, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, info.getLogo() != null ? info.getLogo() : "default-logo.png");
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
            ps.setTimestamp(13, Timestamp.valueOf(now));
            ps.setObject(14, info.getCreatedBy());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            info.setGeneralInfoId(keyHolder.getKey().longValue());
        }
        info.setCreatedAt(now);
        return info;
    }

    @Override
    public GeneralInfo update(GeneralInfo info) {
        String sql = "UPDATE general_info SET logo = ?, company_name = ?, website_name = ?, website_description = ?, email = ?, " +
                     "facebook_link = ?, twitter_link = ?, instagram_link = ?, linkedin_link = ?, youtube_link = ?, zalo_link = ?, " +
                     "company_phone_number = ?, updated_at = ?, updated_by = ? WHERE general_info_id = ?";
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(sql,
                info.getLogo(), info.getCompanyName(), info.getWebsiteName(), info.getWebsiteDescription(), info.getEmail(),
                info.getFacebookLink(), info.getTwitterLink(), info.getInstagramLink(), info.getLinkedinLink(), info.getYoutubeLink(),
                info.getZaloLink(), info.getCompanyPhoneNumber(), Timestamp.valueOf(now), info.getUpdatedBy(), info.getGeneralInfoId()
        );

        info.setUpdatedAt(now);
        return info;
    }
}