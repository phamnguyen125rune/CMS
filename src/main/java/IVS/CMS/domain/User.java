package IVS.CMS.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import IVS.CMS.domain.constants.GenderEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private Long userId;
    private String employeeCode;
    private String fullName;
    private String email;
    private String passwordHash;
    private String avatarUrl;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private GenderEnum gender;
    private String address;
    private Long roleId;

    private Boolean isActive;
    private Boolean isSystem;
    private Integer failedLoginAttempts;
    private Integer lockCount;
    private LocalDateTime lockedUntil;

    private LocalDateTime deletedAt;
    private Long deletedBy;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;

    private Role role;
}