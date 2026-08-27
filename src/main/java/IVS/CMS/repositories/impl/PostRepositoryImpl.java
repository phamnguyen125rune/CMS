package IVS.CMS.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import IVS.CMS.domain.Post;
import IVS.CMS.domain.dto.request.ReqPostFilterDTO;
import IVS.CMS.domain.dto.response.ResPostDTO;
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

    private String buildFilterCondition(ReqPostFilterDTO filter, MapSqlParameterSource params) {
        StringBuilder condition = new StringBuilder(" WHERE 1=1 ");
        if (filter == null)
            return condition.toString();

        if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
            condition.append(" AND (p.title LIKE :keyword OR p.slug LIKE :keyword OR p.summary LIKE :keyword) ");
            params.addValue("keyword", "%" + filter.getKeyword().trim() + "%");
        }
        if (filter.getStatus() != null && !filter.getStatus().trim().isEmpty()) {
            condition.append(" AND p.status = :status ");
            params.addValue("status", filter.getStatus().trim().toUpperCase());
        }
        if (filter.getCategoryId() != null) {
            condition.append(" AND p.category_id = :categoryId ");
            params.addValue("categoryId", filter.getCategoryId());
        }
        if (filter.getAuthorId() != null) {
            condition.append(" AND p.created_by = :authorId ");
            params.addValue("authorId", filter.getAuthorId());
        }
        if (filter.getFromDate() != null) {
            condition.append(" AND DATE(p.created_at) >= :fromDate ");
            params.addValue("fromDate", filter.getFromDate());
        }
        if (filter.getToDate() != null) {
            condition.append(" AND DATE(p.created_at) <= :toDate ");
            params.addValue("toDate", filter.getToDate());
        }

        return condition.toString();
    }

    @Override
    public List<ResPostListDTO> findAll(ReqPostFilterDTO filter, int limit, int offset) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String whereClause = buildFilterCondition(filter, params);

        String sql = """
                SELECT p.post_id, p.title, p.slug, p.summary, p.status, p.published_at, p.created_at, p.created_by, p.updated_at, p.updated_by,
                       p.featured_media_id,
                       c.category_id, c.category_name,
                       uc.full_name AS created_by_name
                FROM posts p
                LEFT JOIN post_categories c ON p.category_id = c.category_id
                LEFT JOIN users uc ON p.created_by = uc.user_id
                """
                + whereClause + " ORDER BY p.created_at DESC LIMIT :limit OFFSET :offset";

        params.addValue("limit", limit).addValue("offset", offset);
        return jdbcTemplate.query(sql, params, mapperDb.rowMapperForListDTO());
    }

    @Override
    public long count(ReqPostFilterDTO filter) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String whereClause = buildFilterCondition(filter, params);

        String sql = "SELECT COUNT(1) FROM posts p " + whereClause;
        Long count = jdbcTemplate.queryForObject(sql, params, Long.class);
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
                    published_at = CASE
                                     WHEN :status = 'PUBLISHED' AND published_at IS NULL THEN NOW(6)
                                     ELSE published_at
                                   END,
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

    @Override
    public void addTagsToPost(long postId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty())
            return;

        String sql = "INSERT IGNORE INTO post_tag (tag_id, post_id) VALUES (:tagId, :postId)";
        MapSqlParameterSource[] batchParams = tagIds.stream()
                .map(tagId -> new MapSqlParameterSource()
                        .addValue("postId", postId)
                        .addValue("tagId", tagId))
                .toArray(MapSqlParameterSource[]::new);
        jdbcTemplate.batchUpdate(sql, batchParams);
    }

    @Override
    public void addMediaToPost(long postId, List<Long> mediaIds) {
        if (mediaIds == null || mediaIds.isEmpty())
            return;

        String sql = "INSERT IGNORE INTO post_media (post_id, media_id, display_order) VALUES (:postId, :mediaId, :displayOrder)";
        MapSqlParameterSource[] batchParams = new MapSqlParameterSource[mediaIds.size()];
        for (int i = 0; i < mediaIds.size(); i++) {
            batchParams[i] = new MapSqlParameterSource()
                    .addValue("postId", postId)
                    .addValue("mediaId", mediaIds.get(i))
                    .addValue("displayOrder", i + 1);
        }
        jdbcTemplate.batchUpdate(sql, batchParams);
    }

    @Override
    public void removeAllTagsFromPost(long postId) {
        String sql = "DELETE FROM post_tag WHERE post_id = :postId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("postId", postId));
    }

    @Override
    public void removeAllMediaFromPost(long postId) {
        String sql = "DELETE FROM post_media WHERE post_id = :postId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("postId", postId));
    }

    @Override
    public List<ResPostDTO.TagInfo> getTagsByPostId(long postId) {
        String sql = """
                SELECT t.tag_id, t.tag_name, t.slug
                FROM tags t
                INNER JOIN post_tag pt ON t.tag_id = pt.tag_id
                WHERE pt.post_id = :postId
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("postId", postId), (rs, rowNum) -> {
            ResPostDTO.TagInfo tag = new ResPostDTO.TagInfo();
            tag.setId(rs.getLong("tag_id"));
            tag.setName(rs.getString("tag_name"));
            tag.setSlug(rs.getString("slug"));
            return tag;
        });
    }

    @Override
    public List<ResPostDTO.MediaInfo> getMediaByPostId(long postId) {
        String sql = """
                SELECT m.media_id, m.file_path, m.file_type, m.file_name
                FROM media m
                INNER JOIN post_media pm ON m.media_id = pm.media_id
                WHERE pm.post_id = :postId
                ORDER BY pm.display_order ASC
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("postId", postId), (rs, rowNum) -> {
            ResPostDTO.MediaInfo media = new ResPostDTO.MediaInfo();
            media.setId(rs.getLong("media_id"));
            media.setFilePath("/api/v1/media/" + rs.getLong("media_id") + "/view");
            media.setFileType(rs.getString("file_type"));
            media.setAltText(rs.getString("file_name"));
            return media;
        });
    }
}