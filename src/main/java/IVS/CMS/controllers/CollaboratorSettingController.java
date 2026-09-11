package IVS.CMS.controllers;

import IVS.CMS.services.CollaboratorSettingService;
import IVS.CMS.services.dto.request.ReqCollaboratorSetting;
import IVS.CMS.services.dto.response.ResCollaboratorSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/collaborator-settings")
@RequiredArgsConstructor
public class CollaboratorSettingController {

    private final CollaboratorSettingService collaboratorSettingService;

    @GetMapping
    public ResCollaboratorSetting getSetting() {
        return collaboratorSettingService.getSetting();
    }

    @PutMapping
    public ResCollaboratorSetting updateSetting(
            @RequestBody ReqCollaboratorSetting request
    ) {
        return collaboratorSettingService.updateSetting(request);
    }
}