package IVS.CMS.services.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReqPermissionApiLinkDTO {
    private List<PermissionLinkDTO> permissions;
}
