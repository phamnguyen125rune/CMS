package IVS.CMS.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import IVS.CMS.services.SecurityService;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Role {
    private long id;

    @NotBlank(message = "không bỏ trống name")
    private String name;

    private String description;
    private boolean active = true;

    private List<Permission> permissions = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityService.getCurrentUserLogin().orElse("system");
    }

    public void handleUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityService.getCurrentUserLogin().orElse("system");
    }
}