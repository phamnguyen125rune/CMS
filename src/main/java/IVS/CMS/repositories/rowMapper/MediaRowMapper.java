package IVS.CMS.repositories.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import IVS.CMS.domain.Media;

@Component
public class MediaRowMapper implements RowMapper<Media> {

    @Override
    public Media mapRow(ResultSet rs,  int rowNum) throws SQLException {
        Media media = new Media();

        media.setId(rs.getLong("id"));
        media.setFileName(rs.getString("file_name"));
        media.setUploadFile(rs.getString("upload_file"));
        media.setFilePath(rs.getString("file_path"));
        media.setMimeType(rs.getString("mime_type"));
        media.setFileType(rs.getString("file_type"));
        media.setFileSize(rs.getInt("file_size"));
        media.setUploadedBy(rs.getString("uploaded_by"));
        
        Timestamp uploadedAt = rs.getTimestamp("uploaded_at");
        if (uploadedAt != null)
            media.setUploadedAt(uploadedAt.toLocalDateTime());

        return media;
    }

    public MapSqlParameterSource toParams(Media media) {
        return new MapSqlParameterSource()
                .addValue("id", media.getId())
                .addValue("fileName", media.getFileName())
                .addValue("uploadFile", media.getUploadFile())
                .addValue("filePath", media.getFilePath())
                .addValue("mimeType", media.getMimeType())
                .addValue("fileType", media.getFileType())
                .addValue("fileSize", media.getFileSize())
                .addValue("uploadedBy", media.getUploadedBy())
                .addValue("uploadedAt",
                        media.getUploadedAt() != null
                                ? Timestamp.valueOf(media.getUploadedAt())
                                : null);
    }
}
