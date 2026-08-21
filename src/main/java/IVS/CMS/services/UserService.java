package IVS.CMS.services;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import IVS.CMS.domain.User;
import IVS.CMS.domain.dto.request.ReqChangePasswordDTO;
import IVS.CMS.domain.dto.request.ReqUpdateProfileDTO;
import IVS.CMS.domain.dto.request.ReqUserCreateDTO;
import IVS.CMS.domain.dto.request.ReqUserUpdateDTO;
import IVS.CMS.domain.dto.response.ResUserCreateDTO;
import IVS.CMS.domain.dto.response.ResUserDTO;
import IVS.CMS.domain.dto.response.ResultPaginationDTO;

public interface UserService {
    ResUserCreateDTO createUser(ReqUserCreateDTO user);

    User fetchUserById(long userId);

    ResUserDTO getUserById(long userId);

    ResultPaginationDTO findAll(int page, int pageSize);

    ResUserDTO updateUser(long userId, ReqUserUpdateDTO req);

    void changePassword(ReqChangePasswordDTO req);

    User handleGetUserByEmail(String email);

    User handleGetUserByEmailOrEmployeeCode(String loginId);

    ResUserCreateDTO register(ReqUserCreateDTO req);

    void softDeleteUser(Long userId);

    void hardDeleteUser(Long userId);

    void restoreUser(Long userId);

    List<ResUserDTO> getDeletedUsers();

    void toggleUserStatus(long userId, Boolean isActive);

    void resetUserPassword(long userId);

    String uploadMyAvatar(MultipartFile file);

    ResUserDTO getMyProfile();

    ResUserDTO updateMyProfile(ReqUpdateProfileDTO req);
}