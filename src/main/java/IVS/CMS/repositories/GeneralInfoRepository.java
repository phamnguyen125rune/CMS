
package IVS.CMS.repositories;

import IVS.CMS.domain.GeneralInfo;
import java.util.Optional;

public interface GeneralInfoRepository {
    Optional<GeneralInfo> findFirst();
    GeneralInfo save(GeneralInfo info);
    GeneralInfo update(GeneralInfo info);
}