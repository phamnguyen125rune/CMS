package IVS.CMS.domain.dto.response;

import java.time.Instant;
import java.time.LocalDate;

import IVS.CMS.domain.constants.GenderEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ResUserDTO {
    private long Id;
    private String fullname;
    private String email;
    private String avatarUrl;
    private String phone;
    private int age;
    private String address;
    private GenderEnum gender;
    private String employeeCode;
    private LocalDate dateOfBirth;
    private String status;
    private RoleUser role;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleUser {
        private long id;
        private String name;
    }
}
