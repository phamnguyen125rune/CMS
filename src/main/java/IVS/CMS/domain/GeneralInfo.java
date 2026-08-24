package IVS.CMS.domain;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class GeneralInfo {
    private Long generalInfoId;
    private String logo;
    private String companyName;
    private String websiteName;
    private String websiteDescription;
    private String email;
    private String facebookLink;
    private String twitterLink;
    private String instagramLink;
    private String linkedinLink;
    private String youtubeLink;
    private String zaloLink;
    private String companyPhoneNumber;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}