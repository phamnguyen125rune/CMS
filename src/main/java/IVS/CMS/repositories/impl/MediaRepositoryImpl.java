package IVS.CMS.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import IVS.CMS.domain.Media;
import IVS.CMS.repositories.MediaRepository;
import IVS.CMS.repositories.rowMapper.MediaRowMapper;

@Repository
public class MediaRepositoryImpl implements MediaRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final MediaRowMapper mapperDb;

    public MediaRepositoryImpl(
            NamedParameterJdbcTemplate jdbcTemplate,
            MediaRowMapper mapperDb) {

        this.jdbcTemplate = jdbcTemplate;
        this.mapperDb = mapperDb;
    }

    @Override
    public Media save(Media media) {

        if (media.getMediaId() == 0) {

            String sql = """
                    INSERT INTO media (
                        file_name,
                        upload_file,
                        file_path,
                        mime_type,
                        file_type,
                        file_size,
                        uploaded_by,
                        uploaded_at
                    )
                    VALUES (
                        :fileName,
                        :uploadFile,
                        :filePath,
                        :mimeType,
                        :fileType,
                        :fileSize,
                        :uploadedBy,
                        :uploadedAt
                    )
                    """;

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(
                    sql,
                    mapperDb.toParams(media),
                    keyHolder,
                    new String[] { "media_id" });

            if (keyHolder.getKey() != null) {
                media.setMediaId(keyHolder.getKey().longValue());
            }

        } else {

            String sql = """
                    UPDATE media
                    SET file_name = :fileName,
                        upload_file = :uploadFile,
                        file_path = :filePath,
                        mime_type = :mimeType,
                        file_type = :fileType,
                        file_size = :fileSize,
                        uploaded_by = :uploadedBy,
                        uploaded_at = :uploadedAt
                    WHERE media_id = :media_id
                    """;

            MapSqlParameterSource params = mapperDb.toParams(media);
            params.addValue("media_id", media.getMediaId());

            jdbcTemplate.update(sql, params);
        }

        return media;
    }

    @Override
    public Optional<Media> findById(long id) {

        String sql = "SELECT * FROM media WHERE media_id = :media_id";

        MapSqlParameterSource params = new MapSqlParameterSource("media_id", id);

        return jdbcTemplate
                .query(sql, params, mapperDb)
                .stream()
                .findFirst();
    }

    @Override
    public List<Media> findAll() {

        String sql = """
                SELECT *
                FROM media
                ORDER BY uploaded_at DESC
                """;

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource(),
                mapperDb);
    }


    private void appendFileTypeCondition(
            StringBuilder sql,
            String fileType) {

        if (fileType == null
                || fileType.trim().isEmpty()
                || "all".equalsIgnoreCase(fileType)) {
            return;
        }

        switch (fileType.toLowerCase()) {

            case "image":
                sql.append("""
                        AND mime_type LIKE 'image/%'
                        """);
                break;

            case "pdf":
                sql.append("""
                        AND mime_type = 'application/pdf'
                        """);
                break;

            case "word":
                sql.append("""
                        AND mime_type IN (
                            'application/msword',
                            'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
                        )
                        """);
                break;

            case "excel":
                sql.append("""
                        AND mime_type IN (
                            'application/vnd.ms-excel',
                            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
                        )
                        """);
                break;

            case "other":
                sql.append("""
                        AND mime_type NOT LIKE 'image/%'
                        AND mime_type NOT IN (
                            'application/pdf',
                            'application/msword',
                            'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
                            'application/vnd.ms-excel',
                            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
                        )
                        """);
                break;

            default:
                break;
        }
    }

    @Override
    public List<Media> searchAndFilter(
            String keyword,
            String fileType) {

        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM media
                WHERE 1 = 1
                """);

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (keyword != null && !keyword.trim().isEmpty()) {

            sql.append("""
                    AND (
                        LOWER(file_name) LIKE LOWER(:keyword)
                        OR LOWER(upload_file) LIKE LOWER(:keyword)
                        OR LOWER(uploaded_by) LIKE LOWER(:keyword)
                        OR DATE_FORMAT(uploaded_at, '%d/%m/%Y') LIKE :keyword
                        OR DATE_FORMAT(uploaded_at, '%Y-%m-%d') LIKE :keyword
                    )
                    """);

            params.addValue("keyword", "%" + keyword.trim() + "%");
        }

        appendFileTypeCondition(sql, fileType);

        sql.append("""
                ORDER BY uploaded_at DESC
                                """);

        return jdbcTemplate.query(sql.toString(), params, mapperDb);
    }

    @Override
    public long count() {

        String sql = "SELECT COUNT(*) FROM media";

        Long count = jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource(),
                Long.class);

        return count != null ? count : 0;
    }

    @Override
    public void delete(Media media) {

        String sql = "DELETE FROM media WHERE media_id = :media_id";

        MapSqlParameterSource params = new MapSqlParameterSource("media_id", media.getMediaId());

        jdbcTemplate.update(sql, params);
    }
}