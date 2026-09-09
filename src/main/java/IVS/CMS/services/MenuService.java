package IVS.CMS.services;

import IVS.CMS.services.dto.request.ReqMenuDTO;
import IVS.CMS.services.dto.response.ResMenuDTO;

import java.util.List;

public interface MenuService {
    List<ResMenuDTO> getMenu();

    ResMenuDTO findById(Long id);

    ResMenuDTO create(ReqMenuDTO request);

    ResMenuDTO update(Long id, ReqMenuDTO request);

    void delete(Long id);
}