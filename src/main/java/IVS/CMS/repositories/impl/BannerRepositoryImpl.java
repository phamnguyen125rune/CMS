package IVS.CMS.repositories.impl;

import IVS.CMS.domain.Banner;
import IVS.CMS.repositories.BannerRepository;
import IVS.CMS.repositories.rowMapper.BannerRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class BannerRepositoryImpl implements BannerRepository {

    private final JdbcTemplate jdbcTemplate;
    private final BannerRowMapper rowMapper;

    public BannerRepositoryImpl(JdbcTemplate jdbcTemplate, BannerRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    @Override
    public List<Banner> findActiveByPosition(String position) {
        if (position == null || position.trim().isEmpty() || "ALL".equalsIgnoreCase(position.trim())) {
            String sql = "SELECT * FROM banners WHERE is_active = TRUE ORDER BY display_order ASC, banner_id DESC";
            return jdbcTemplate.query(sql, rowMapper);
        }
        String sql = "SELECT * FROM banners WHERE is_active = TRUE AND position = ? ORDER BY display_order ASC, banner_id DESC";
        return jdbcTemplate.query(sql, rowMapper, position.trim());
    }

    @Override
    public List<Banner> findAll(String search, String position, Boolean isActive, int page, int size) {
        StringBuilder sql = new StringBuilder("SELECT * FROM banners WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (title LIKE ? OR subtitle LIKE ? OR description LIKE ?) ");
            String keyword = "%" + search.trim() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        if (position != null && !position.trim().isEmpty()) {
            sql.append("AND position = ? ");
            params.add(position.trim());
        }

        if (isActive != null) {
            sql.append("AND is_active = ? ");
            params.add(isActive);
        }

        sql.append("ORDER BY display_order ASC, banner_id DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add((page - 1) * size);

        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    @Override
    public long count(String search, String position, Boolean isActive) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM banners WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (title LIKE ? OR subtitle LIKE ? OR description LIKE ?) ");
            String keyword = "%" + search.trim() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        if (position != null && !position.trim().isEmpty()) {
            sql.append("AND position = ? ");
            params.add(position.trim());
        }

        if (isActive != null) {
            sql.append("AND is_active = ? ");
            params.add(isActive);
        }

        Long total = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return total != null ? total : 0L;
    }

    @Override
    public Optional<Banner> findById(Long id) {
        String sql = "SELECT * FROM banners WHERE banner_id = ?";
        List<Banner> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.stream().findFirst();
    }

    @Override
    public Banner save(Banner banner) {
        String sql = "INSERT INTO banners (title, highlight_text, subtitle, description, image_url, mobile_image_url, " +
                "primary_btn_text, primary_btn_url, secondary_btn_text, secondary_btn_url, stats_json, floating_badge_text, " +
                "position, display_order, is_active, created_at, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, banner.getTitle());
            ps.setString(2, banner.getHighlightText());
            ps.setString(3, banner.getSubtitle());
            ps.setString(4, banner.getDescription());
            ps.setString(5, banner.getImageUrl());
            ps.setString(6, banner.getMobileImageUrl());
            ps.setString(7, banner.getPrimaryBtnText());
            ps.setString(8, banner.getPrimaryBtnUrl());
            ps.setString(9, banner.getSecondaryBtnText());
            ps.setString(10, banner.getSecondaryBtnUrl());
            ps.setString(11, banner.getStatsJson());
            ps.setString(12, banner.getFloatingBadgeText());
            ps.setString(13, banner.getPosition() != null ? banner.getPosition() : "HOME_HERO");
            ps.setInt(14, banner.getDisplayOrder() != null ? banner.getDisplayOrder() : 0);
            ps.setBoolean(15, banner.getIsActive() != null ? banner.getIsActive() : true);
            ps.setTimestamp(16, Timestamp.valueOf(now));
            if (banner.getCreatedBy() != null) {
                ps.setLong(17, banner.getCreatedBy());
            } else {
                ps.setNull(17, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            banner.setBannerId(keyHolder.getKey().longValue());
        }
        banner.setCreatedAt(now);
        return banner;
    }

    @Override
    public Banner update(Banner banner) {
        String sql = "UPDATE banners SET title = ?, highlight_text = ?, subtitle = ?, description = ?, " +
                "image_url = ?, mobile_image_url = ?, primary_btn_text = ?, primary_btn_url = ?, " +
                "secondary_btn_text = ?, secondary_btn_url = ?, stats_json = ?, floating_badge_text = ?, " +
                "position = ?, display_order = ?, is_active = ?, updated_at = ?, updated_by = ? " +
                "WHERE banner_id = ?";

        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(sql,
                banner.getTitle(),
                banner.getHighlightText(),
                banner.getSubtitle(),
                banner.getDescription(),
                banner.getImageUrl(),
                banner.getMobileImageUrl(),
                banner.getPrimaryBtnText(),
                banner.getPrimaryBtnUrl(),
                banner.getSecondaryBtnText(),
                banner.getSecondaryBtnUrl(),
                banner.getStatsJson(),
                banner.getFloatingBadgeText(),
                banner.getPosition(),
                banner.getDisplayOrder(),
                banner.getIsActive(),
                Timestamp.valueOf(now),
                banner.getUpdatedBy(),
                banner.getBannerId());

        banner.setUpdatedAt(now);
        return banner;
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM banners WHERE banner_id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public boolean updateStatus(Long id, boolean isActive) {
        String sql = "UPDATE banners SET is_active = ?, updated_at = ? WHERE banner_id = ?";
        return jdbcTemplate.update(sql, isActive, Timestamp.valueOf(LocalDateTime.now()), id) > 0;
    }
}
