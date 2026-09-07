package IVS.CMS.services.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ReqUpdateGeneralInfoDTO {
    private String logo;
    @NotBlank(message = "Tên công ty không được để trống")
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

    // Getters and Setters
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getWebsiteName() { return websiteName; }
    public void setWebsiteName(String websiteName) { this.websiteName = websiteName; }
    public String getWebsiteDescription() { return websiteDescription; }
    public void setWebsiteDescription(String websiteDescription) { this.websiteDescription = websiteDescription; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFacebookLink() { return facebookLink; }
    public void setFacebookLink(String facebookLink) { this.facebookLink = facebookLink; }
    public String getTwitterLink() { return twitterLink; }
    public void setTwitterLink(String twitterLink) { this.twitterLink = twitterLink; }
    public String getInstagramLink() { return instagramLink; }
    public void setInstagramLink(String instagramLink) { this.instagramLink = instagramLink; }
    public String getLinkedinLink() { return linkedinLink; }
    public void setLinkedinLink(String linkedinLink) { this.linkedinLink = linkedinLink; }
    public String getYoutubeLink() { return youtubeLink; }
    public void setYoutubeLink(String youtubeLink) { this.youtubeLink = youtubeLink; }
    public String getZaloLink() { return zaloLink; }
    public void setZaloLink(String zaloLink) { this.zaloLink = zaloLink; }
    public String getCompanyPhoneNumber() { return companyPhoneNumber; }
    public void setCompanyPhoneNumber(String companyPhoneNumber) { this.companyPhoneNumber = companyPhoneNumber; }
}