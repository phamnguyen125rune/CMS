package IVS.CMS.services.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PermissionLinkDTO {
    private String apiLink;
    private String actionName;
}
