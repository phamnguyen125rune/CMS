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

    public ResUserCreateDTO createUser(ReqUserCreateDTO user);

    public User fetchUserById(long id);

    public ResUserDTO getUserById(long id);

    public ResultPaginationDTO findAll(int page, int pageSize);

    public ReqUserUpdateDTO UpdateUser(long id, ReqUserUpdateDTO req);

    public void changePassword(ReqChangePasswordDTO req);

    public User handleGetUserByEmail(String username);

    void updateUserToken(String token, String email);

    public User getUserByRefreshTokenAndEmail(String refreshToken, String email);

    public ResUserCreateDTO register(ReqUserCreateDTO req);

    void softDeleteUser(Long id);

    void hardDeleteUser(Long id);

    void restoreUser(Long id);

    List<ResUserDTO> getDeletedUsers();

    void toggleUserStatus(long id, String status);

    void resetUserPassword(long id);

    String uploadMyAvatar(MultipartFile file);

    ResUserDTO getMyProfile();

    ResUserDTO updateMyProfile(ReqUpdateProfileDTO req);

    User handleGetUserByEmailOrEmployeeCode(String loginId);
}
