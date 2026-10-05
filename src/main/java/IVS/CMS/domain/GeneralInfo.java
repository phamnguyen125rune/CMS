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
    private String companyPhoneNumber;
    private String address;
    private String facebookLink;
    private String twitterLink;
    private String instagramLink;
    private String linkedinLink;
    private String youtubeLink;
    private String zaloLink;
    private String workingHours;
    private String mapEmbedUrl;
    private String footerLinks;
    // Header customization
    private Boolean showTopbar;
    private String topbarAnnouncementText;
    private String topbarAnnouncementUrl;
    private String headerCtaText;
    private String headerCtaUrl;
    private Boolean showHeaderSearch;
    private Boolean showThemeToggle;
    private Boolean showLanguageSwitch;
    // Footer customization
    private String footerCopyright;
    private Boolean showNewsletter;
    private String newsletterTitle;
    private String newsletterDesc;
    private String footerColumnsJson;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}