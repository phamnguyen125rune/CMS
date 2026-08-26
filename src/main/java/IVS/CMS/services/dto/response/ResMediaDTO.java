package IVS.CMS.services.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResMediaDTO {
    private long mediaId;
    private String fileName;
    private String filePath;
    private String mimeType;
    private String fileType;
    private int fileSize;
    private Long uploadedBy;
    private LocalDateTime uploadedAt;
}
