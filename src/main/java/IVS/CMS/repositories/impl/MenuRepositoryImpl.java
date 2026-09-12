package IVS.CMS.repositories.impl;

import IVS.CMS.domain.Menu;
import IVS.CMS.repositories.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<Menu> getMenu() {
        String sql = """
                SELECT menu_id, parent_id, title, url, display_order, level,
                        visible, created_at, created_by, updated_at, updated_by
                FROM menu
                ORDER BY display_order ASC
                """;

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Menu.class));
    }

    @Override
    public Optional<Menu> findById(Long id) {
        String sql = """
                SELECT menu_id, parent_id, title, url, display_order, level,
                        visible, created_at, created_by, updated_at, updated_by
                FROM menu
                WHERE menu_id = :menuId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("menuId", id);

        List<Menu> result = jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Menu.class));

        return result.stream().findFirst();
    }

    @Override
    public int save(Menu menu) {
        String sql = """
                INSERT INTO menu (
                    parent_id, title, url, display_order, level,
                    visible, created_at, created_by
                )
                VALUES (
                    :parentId, :title, :url,  :displayOrder, :level,
                    :visible, :createdAt, :createdBy
                )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("parentId", menu.getParentId())
                .addValue("title", menu.getTitle())
                .addValue("url", menu.getUrl())
                .addValue("displayOrder", menu.getDisplayOrder())
                .addValue("level", menu.getLevel())
                .addValue("visible", menu.getVisible())
                .addValue("createdAt", menu.getCreatedAt())
                .addValue("createdBy", menu.getCreatedBy());

        return jdbcTemplate.update(sql, params);
    }

    @Override
    public int update(Menu menu) {
        String sql = """
                UPDATE menu
                SET parent_id = :parentId,
                    title = :title,
                    url = :url,
                    display_order = :displayOrder,
                    level = :level,
                    visible = :visible,
                    updated_at = :updatedAt,
                    updated_by = :updatedBy
                WHERE menu_id = :menuId
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("menuId", menu.getMenuId())
                .addValue("parentId", menu.getParentId())
                .addValue("title", menu.getTitle())
                .addValue("url", menu.getUrl())
                .addValue("displayOrder", menu.getDisplayOrder())
                .addValue("level", menu.getLevel())
                .addValue("visible", menu.getVisible())
                .addValue("updatedAt", menu.getUpdatedAt())
                .addValue("updatedBy", menu.getUpdatedBy());

        return jdbcTemplate.update(sql, params);
    }

    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM menu WHERE menu_id = :menuId";

        return jdbcTemplate.update(sql, new MapSqlParameterSource("menuId", id));
    }
}