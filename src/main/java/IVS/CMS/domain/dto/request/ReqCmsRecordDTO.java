package IVS.CMS.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCmsRecordDTO {
    @NotBlank(message = "Module không được để trống")
    private String moduleKey;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String subtitle;
    private String type;
    private String status;
    private String owner;
    private String description;
    private String imageUrl;
}
