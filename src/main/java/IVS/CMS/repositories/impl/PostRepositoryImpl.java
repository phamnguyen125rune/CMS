package IVS.CMS.repositories.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import IVS.CMS.domain.Post;
import IVS.CMS.domain.dto.response.ResPostListDTO;
import IVS.CMS.repositories.PostRepository;
import IVS.CMS.repositories.rowMapper.PostRowMapper;

@Repository
public class PostRepositoryImpl implements PostRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PostRowMapper mapperDb;

    public PostRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate, PostRowMapper mapperDb) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapperDb = mapperDb;
    }

    @Override
    public Post save(Post post) {
        if (post.getPostId() == null || post.getPostId() == 0) {
            String sql = """
                    INSERT INTO posts (
                        title, slug, summary, content,
                        meta_title, meta_description, canonical_url, is_indexable, is_followable,
                        og_title, og_description, og_image_id, featured_media_id,
                        status, category_id, published_at, created_at, created_by, updated_at, updated_by
                    ) VALUES (
                        :title, :slug, :summary, :content,
                        :metaTitle, :metaDescription, :canonicalUrl, :isIndexable, :isFollowable,
                        :ogTitle, :ogDescription, :ogImageId, :featuredMediaId,
                        :status, :categoryId, :publishedAt, :createdAt, :createdBy, :updatedAt, :updatedBy
                    )
                    """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(sql, mapperDb.toParams(post), keyHolder, new String[] { "post_id" });
            if (keyHolder.getKey() != null) {
                post.setPostId(keyHolder.getKey().longValue());
            }
        } else {
            String sql = """
                    UPDATE posts
                    SET title = :title,
                        slug = :slug,
                        summary = :summary,
                        content = :content,
                        meta_title = :metaTitle,
                        meta_description = :metaDescription,
                        canonical_url = :canonicalUrl,
                        is_indexable = :isIndexable,
                        is_followable = :isFollowable,
                        og_title = :ogTitle,
                        og_description = :ogDescription,
                        og_image_id = :ogImageId,
                        featured_media_id = :featuredMediaId,
                        status = :status,
                        category_id = :categoryId,
                        published_at = :publishedAt,
                        updated_at = :updatedAt,
                        updated_by = :updatedBy
                    WHERE post_id = :postId
                    """;
            jdbcTemplate.update(sql, mapperDb.toParams(post));
        }
        return post;
    }

    @Override
    public Optional<Post> findById(long id) {
        String sql = "SELECT * FROM posts WHERE post_id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        return jdbcTemplate.query(sql, params, mapperDb).stream().findFirst();
    }

    @Override
    public List<ResPostListDTO> findAll(int limit, int offset) {
        String sql = """
                SELECT p.post_id, p.title, p.slug, p.summary, p.status, p.published_at, p.created_at, p.created_by, p.updated_at, p.updated_by, p.featured_media_id,
                       c.category_id, c.category_name,
                       uc.full_name AS created_by_name
                FROM posts p
                LEFT JOIN post_categories c ON p.category_id = c.category_id
                LEFT JOIN users uc ON p.created_by = uc.user_id
                ORDER BY p.created_at DESC
                LIMIT :limit OFFSET :offset
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);
        return jdbcTemplate.query(sql, params, mapperDb.rowMapperForListDTO());
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(1) FROM posts";
        Long count = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM posts WHERE post_id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

    @Override
    public boolean existsBySlug(String slug) {
        String sql = "SELECT COUNT(1) FROM posts WHERE slug = :slug";
        MapSqlParameterSource params = new MapSqlParameterSource("slug", slug);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsBySlugForUpdate(long id, String slug) {
        String sql = "SELECT COUNT(1) FROM posts WHERE slug = :slug AND post_id != :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("slug", slug)
                .addValue("id", id);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public void updateStatus(long id, String status, Long updatedBy) {
        String sql = """
                UPDATE posts
                SET status = :status,
                    updated_at = NOW(6),
                    updated_by = :updatedBy
                WHERE post_id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("status", status)
                .addValue("updatedBy", updatedBy);
        jdbcTemplate.update(sql, params);
    }
}