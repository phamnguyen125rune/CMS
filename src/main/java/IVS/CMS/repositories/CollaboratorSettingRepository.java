package IVS.CMS.repositories;

import IVS.CMS.domain.CollaboratorSetting;

import java.util.Optional;

public interface CollaboratorSettingRepository {

    Optional<CollaboratorSetting> find();

    CollaboratorSetting update(Integer columnsPerRow, Long updatedBy);
}