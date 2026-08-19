package IVS.CMS.domain;

import java.time.Instant;
import java.time.LocalDate;

import IVS.CMS.domain.constants.GenderEnum;
import IVS.CMS.services.SecurityService;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

    private long id;

    private String fullname;

    private String email;

    private String password;

    private String avatarUrl;
    private String refreshToken;
    private String phone;
    private int age;
    private String address;
    private GenderEnum gender;

    private String employeeCode;
    private LocalDate dateOfBirth;
    private String status;
    private int failedLoginAttempts;
    private int lockCount;
    private Instant lockedUntil;

    private Boolean deleted;
    private Instant deletedAt;
    private String deletedBy;

    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    private Role role;

    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityService.getCurrentUserLogin().orElse("");
    }

    public void handleUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityService.getCurrentUserLogin().orElse("");

    }
}
