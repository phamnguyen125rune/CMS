package IVS.CMS.services.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    private String workingHours;

    private String mapEmbedUrl;

    private String footerLinks;
}