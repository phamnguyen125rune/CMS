package IVS.CMS.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class Tag {

    private Long tagId;
    @NotBlank(message = "Tên thẻ (Tag) không được để trống")
    private String tagName;

    private LocalDateTime createdAt;
    private Long createdBy;

    private LocalDateTime updatedAt;
    private Long updatedBy;
}