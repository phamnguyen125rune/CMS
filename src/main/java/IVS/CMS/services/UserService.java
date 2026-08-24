package IVS.CMS.services;

import IVS.CMS.domain.User;
import IVS.CMS.services.dto.request.ReqChangePasswordDTO;
import IVS.CMS.services.dto.request.ReqUserCreateDTO;
import IVS.CMS.services.dto.request.ReqUserUpdateDTO;
import IVS.CMS.services.dto.response.ResUserCreateDTO;
import IVS.CMS.services.dto.response.ResUserDTO;
import IVS.CMS.services.dto.response.ResultPaginationDTO;

public interface UserService {
    ResUserCreateDTO createUser(ReqUserCreateDTO user);

    User fetchUserById(long userId);

    ResUserDTO getUserById(long userId);

    ResultPaginationDTO findAll(int page, int pageSize);

    ResUserDTO updateUser(long userId, ReqUserUpdateDTO req);

    User handleGetUserByEmailOrEmployeeCode(String loginId);

    ResUserCreateDTO register(ReqUserCreateDTO req);

    void deleteUser(long userId);

    void updateStatus(long userId, Boolean isActive);

    void changePassword(ReqChangePasswordDTO req);
}