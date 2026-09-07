package IVS.CMS.services;

import java.util.List;

import IVS.CMS.services.dto.request.role.ReqPermissionApiLinkDTO;
import IVS.CMS.services.dto.request.role.ReqPermissionIdDTO;
import IVS.CMS.services.dto.response.role.ResApiActionDTO;


public interface PermissionService {
    List<ResApiActionDTO> getAllApiActions();

    public String assignPermissionToRoleById(long roleId, ReqPermissionIdDTO req);

    String assignPermissionToRoleByApiLink(long roleId, ReqPermissionApiLinkDTO req);
}