package IVS.CMS.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Role {
    private long roleId;

    @NotBlank(message = "Tên role không được để trống")
    private String roleName;

    private String roleDescription;
    private Boolean isActive;
    private Boolean isSystem;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private List<Permission> permissions = new ArrayList<>();
}