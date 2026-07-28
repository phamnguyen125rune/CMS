package IVS.CMS.domain;

import java.time.Instant;
import java.util.Locale;

import IVS.CMS.services.SecurityService;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Permission {
    private long id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Resource code cannot be blank")
    private String resourceCode;

    @NotBlank(message = "Action cannot be blank")
    private String action;

    private String permissionCode;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    public Permission() {
    }

    public Permission(String name, String resourceCode, String action) {
        this.name = name;
        this.resourceCode = resourceCode;
        this.action = action;
        normalizePermissionCode();
    }

    public void normalizePermissionCode() {
        if (this.resourceCode != null) {
            this.resourceCode = this.resourceCode.trim().toLowerCase(Locale.ROOT);
        }
        if (this.action != null) {
            this.action = this.action.trim().toUpperCase(Locale.ROOT);
        }
        if (this.permissionCode == null || this.permissionCode.isBlank()) {
            if (this.resourceCode != null && !this.resourceCode.isBlank()
                    && this.action != null && !this.action.isBlank()) {
                this.permissionCode = this.resourceCode + ":" + this.action;
            }
        } else {
            this.permissionCode = this.permissionCode.trim();
        }
    }

    public void handleBeforeCreate() {
        normalizePermissionCode();
        this.createdAt = Instant.now();
        this.createdBy = SecurityService.getCurrentUserLogin().orElse("system");
    }

    public void handleUpdate() {
        normalizePermissionCode();
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityService.getCurrentUserLogin().orElse("system");
    }
}
