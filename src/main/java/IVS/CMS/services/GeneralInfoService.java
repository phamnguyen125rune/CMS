package IVS.CMS.services;

import IVS.CMS.domain.GeneralInfo;
import IVS.CMS.services.dto.request.ReqUpdateGeneralInfoDTO;

public interface GeneralInfoService {
    GeneralInfo getGeneralInfo();
    GeneralInfo saveOrUpdateGeneralInfo(ReqUpdateGeneralInfoDTO dto, Long userId);
}