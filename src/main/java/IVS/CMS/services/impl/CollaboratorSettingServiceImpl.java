package IVS.CMS.services.impl;

import IVS.CMS.domain.CollaboratorSetting;
import IVS.CMS.repositories.CollaboratorSettingRepository;
import IVS.CMS.services.CollaboratorSettingService;
import IVS.CMS.services.dto.request.ReqCollaboratorSetting;
import IVS.CMS.services.dto.response.ResCollaboratorSetting;
import IVS.CMS.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CollaboratorSettingServiceImpl implements CollaboratorSettingService {

    private final CollaboratorSettingRepository collaboratorSettingRepository;
    private final SecurityService securityService;

    @Override
    public ResCollaboratorSetting getSetting() {
        CollaboratorSetting setting = collaboratorSettingRepository.find()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình collaborator"));

        return toResponse(setting);
    }

    @Override
    public ResCollaboratorSetting updateSetting(ReqCollaboratorSetting request) {

        if (request.getColumnsPerRow() == null ||
                request.getColumnsPerRow() < 2 ||
                request.getColumnsPerRow() > 6) {
            throw new RuntimeException("Số công ty mỗi dòng phải từ 2 đến 6");
        }

        Long updatedBy = securityService.getCurrentUserId().orElse(null);

        CollaboratorSetting setting =
                collaboratorSettingRepository.update(
                        request.getColumnsPerRow(),
                        updatedBy
                );

        return toResponse(setting);
    }

    private ResCollaboratorSetting toResponse(CollaboratorSetting setting) {
        ResCollaboratorSetting response = new ResCollaboratorSetting();

        response.setSettingId(setting.getSettingId());
        response.setColumnsPerRow(setting.getColumnsPerRow());
        response.setUpdatedAt(setting.getUpdatedAt());
        response.setUpdatedBy(setting.getUpdatedBy());

        return response;
    }
}