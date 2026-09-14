package IVS.CMS.services.dto.response.role;

import java.util.List;

import IVS.CMS.domain.Action;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResApiActionDTO {
    private Long apiId;
    private String apiLink;
    private String apiDescription;
    private List<Action> actions;
}
