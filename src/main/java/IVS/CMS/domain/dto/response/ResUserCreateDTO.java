package IVS.CMS.domain.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import IVS.CMS.domain.constants.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ResUserCreateDTO {
    private Long userId;
    private String employeeCode;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private GenderEnum gender;
    private String address;
    private Boolean isActive;
    private RoleUser role;
    private LocalDateTime createdAt;
    private Long createdBy;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleUser {
        private Long roleId;
        private String roleName;
    }
}