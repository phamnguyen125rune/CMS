package IVS.CMS.services;

import java.util.List;

import IVS.CMS.domain.Api;
import IVS.CMS.services.dto.request.ReqPermissionApiLinkDTO;
import IVS.CMS.services.dto.request.ReqPermissionIdDTO;
import IVS.CMS.services.dto.response.ResActionDTO;

public interface PermissionService {
    List<ResActionDTO> getAllActions();

    List<Api> getAllApis();

    public String assignPermissionToRoleById(long roleId, ReqPermissionIdDTO req);

    String assignPermissionToRoleByApiLink(long roleId, ReqPermissionApiLinkDTO req);
}