package IVS.CMS.domain;

import java.time.Instant;

import IVS.CMS.services.SecurityService;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CmsRecord {
    private long id;
    private String moduleKey;
    private String title;
    private String subtitle;
    private String type;
    private String status;
    private String owner;
    private String description;
    private String imageUrl;
    private Boolean deleted;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityService.getCurrentUserLogin().orElse("system");
        this.updatedAt = this.createdAt;
        this.updatedBy = this.createdBy;
        this.deleted = false;
    }

    public void handleUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityService.getCurrentUserLogin().orElse("system");
    }
}
