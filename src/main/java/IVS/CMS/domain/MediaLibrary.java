package IVS.CMS.domain;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class MediaLibrary {
    private Long mediaId;

    @NotBlank(message = "Tên file không được để trống")
    private String fileName;

    @NotBlank(message = "Tên file gốc không được để trống")
    private String uploadFileName;

    @NotBlank(message = "Đường dẫn file không được để trống")
    private String filePath;

    @NotBlank(message = "Mime type không được để trống")
    private String mimeType;

    @NotBlank(message = "Loại file không được để trống")
    private String fileType;

    @NotNull(message = "Kích thước file không được trống")
    private Long fileSize;

    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
}