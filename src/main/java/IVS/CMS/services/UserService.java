package IVS.CMS.services;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import IVS.CMS.domain.User;
import IVS.CMS.services.dto.request.ReqChangePasswordDTO;
import IVS.CMS.services.dto.request.ReqUpdateProfileDTO;
import IVS.CMS.services.dto.request.ReqUserCreateDTO;
import IVS.CMS.services.dto.request.ReqUserUpdateDTO;
import IVS.CMS.services.dto.response.ResUserCreateDTO;
import IVS.CMS.services.dto.response.ResUserDTO;
import IVS.CMS.services.dto.response.ResultPaginationDTO;

public interface UserService {
    ResUserCreateDTO createUser(ReqUserCreateDTO user);

    User fetchUserById(long id);

    ResUserDTO getUserById(long id);

    ResultPaginationDTO findAll(int page, int pageSize);

    ReqUserUpdateDTO UpdateUser(long id, ReqUserUpdateDTO req);

    void changePassword(ReqChangePasswordDTO req);

    User handleGetUserByEmail(String username);

    User handleGetUserByEmailOrEmployeeCode(String loginId);

    User handleGetUserByEmailOrEmployeeCodeIncludeDeleted(String loginId);

    ResUserCreateDTO register(ReqUserCreateDTO req);

    void softDeleteUser(Long id);

    void hardDeleteUser(Long id);

    void restoreUser(Long id);

    List<ResUserDTO> getDeletedUsers();

    void toggleUserStatus(long id, String status);

    void resetUserPassword(long id);

    void resetPasswordByEmail(String email, String newPassword);

    String recordFailedLogin(User user);

    void clearLoginFailures(long id);

    void resetLoginSecurity(long id);

    String uploadMyAvatar(MultipartFile file);

    String uploadUserAvatar(long id, MultipartFile file);

    ResUserDTO getMyProfile();

    ResUserDTO updateMyProfile(ReqUpdateProfileDTO req);
}