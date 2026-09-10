package IVS.CMS.services.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ReqUpdateGeneralInfoDTO {

    @NotBlank(message = "Logo không được để trống")
    @Size(max = 255, message = "Logo không được vượt quá 255 ký tự")
    private String logo;

    @NotBlank(message = "Tên công ty không được để trống")
    @Size(max = 255, message = "Tên công ty không được vượt quá 255 ký tự")
    private String companyName;

    @Size(max = 60, message = "Tên website không được vượt quá 60 ký tự")
    private String websiteName;

    private String websiteDescription;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    private String email;

    @Pattern(regexp = "^$|https?://.+", message = "Facebook link không đúng định dạng URL")
    @Size(max = 255, message = "Facebook link không được vượt quá 255 ký tự")
    private String facebookLink;

    @Pattern(regexp = "^$|https?://.+", message = "Twitter link không đúng định dạng URL")
    @Size(max = 255, message = "Twitter link không được vượt quá 255 ký tự")
    private String twitterLink;

    @Pattern(regexp = "^$|https?://.+", message = "Instagram link không đúng định dạng URL")
    @Size(max = 255, message = "Instagram link không được vượt quá 255 ký tự")
    private String instagramLink;

    @Pattern(regexp = "^$|https?://.+", message = "LinkedIn link không đúng định dạng URL")
    @Size(max = 255, message = "LinkedIn link không được vượt quá 255 ký tự")
    private String linkedinLink;

    @Pattern(regexp = "^$|https?://.+", message = "YouTube link không đúng định dạng URL")
    @Size(max = 255, message = "YouTube link không được vượt quá 255 ký tự")
    private String youtubeLink;

    @Pattern(regexp = "^$|https?://.+", message = "Zalo link không đúng định dạng URL")
    @Size(max = 255, message = "Zalo link không được vượt quá 255 ký tự")
    private String zaloLink;

    @Pattern(regexp = "^$|\\+?[0-9\\s().-]{8,20}$", message = "Số điện thoại không đúng định dạng")
    @Size(max = 255, message = "Số điện thoại không được vượt quá 255 ký tự")
    private String companyPhoneNumber;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    private String footerLinks;

    // =========================
    // Getters and Setters
    // =========================

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getWebsiteName() {
        return websiteName;
    }

    public void setWebsiteName(String websiteName) {
        this.websiteName = websiteName;
    }

    public String getWebsiteDescription() {
        return websiteDescription;
    }

    public void setWebsiteDescription(String websiteDescription) {
        this.websiteDescription = websiteDescription;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFacebookLink() {
        return facebookLink;
    }

    public void setFacebookLink(String facebookLink) {
        this.facebookLink = facebookLink;
    }

    public String getTwitterLink() {
        return twitterLink;
    }

    public void setTwitterLink(String twitterLink) {
        this.twitterLink = twitterLink;
    }

    public String getInstagramLink() {
        return instagramLink;
    }

    public void setInstagramLink(String instagramLink) {
        this.instagramLink = instagramLink;
    }

    public String getLinkedinLink() {
        return linkedinLink;
    }

    public void setLinkedinLink(String linkedinLink) {
        this.linkedinLink = linkedinLink;
    }

    public String getYoutubeLink() {
        return youtubeLink;
    }

    public void setYoutubeLink(String youtubeLink) {
        this.youtubeLink = youtubeLink;
    }

    public String getZaloLink() {
        return zaloLink;
    }

    public void setZaloLink(String zaloLink) {
        this.zaloLink = zaloLink;
    }

    public String getCompanyPhoneNumber() {
        return companyPhoneNumber;
    }

    public void setCompanyPhoneNumber(String companyPhoneNumber) {
        this.companyPhoneNumber = companyPhoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFooterLinks() {
        return footerLinks;
    }

    public void setFooterLinks(String footerLinks) {
        this.footerLinks = footerLinks;
    }
}