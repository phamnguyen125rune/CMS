package IVS.CMS.domain;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Media {
    private long id;
    @NotBlank(message = "File name cannot be blank")
    private String fileName;
    private String uploadFile;
    @NotBlank(message = "File path cannot be blank")
    private String filePath;
    @NotBlank(message = "Mime type cannot be blank")
    private String mimeType;
    @NotBlank(message = "File type cannot be blank")
    private String fileType;
    @NotNull(message = "File size cannot be blank")
    private int fileSize;
    @NotBlank(message = "Uploaded by cannot be blank")
    private String uploadedBy;
    private LocalDateTime uploadedAt;
}
