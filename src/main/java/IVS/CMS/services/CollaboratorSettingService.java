package IVS.CMS.services;

import IVS.CMS.services.dto.request.ReqCollaboratorSetting;
import IVS.CMS.services.dto.response.ResCollaboratorSetting;

public interface CollaboratorSettingService {

    ResCollaboratorSetting getSetting();

    ResCollaboratorSetting updateSetting(
            ReqCollaboratorSetting request
    );
}