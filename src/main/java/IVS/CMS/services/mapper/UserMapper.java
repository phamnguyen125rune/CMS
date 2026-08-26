package IVS.CMS.services.mapper;

import org.springframework.stereotype.Component;

import IVS.CMS.domain.User;
import IVS.CMS.services.dto.request.ReqUserCreateDTO;
import IVS.CMS.services.dto.request.ReqUserUpdateDTO;
import IVS.CMS.services.dto.response.ResUserCreateDTO;
import IVS.CMS.services.dto.response.ResUserDTO;

@Component
public class UserMapper {

    public User reqCreateToUser(ReqUserCreateDTO dto) {
        if (dto == null)
            return null;
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());
        user.setGender(dto.getGender());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setRoleId(dto.getRoleId());
        return user;
    }

    public ResUserCreateDTO userToResCreateDTO(User user) {
        if (user == null)
            return null;
        ResUserCreateDTO res = new ResUserCreateDTO();
        res.setUserId(user.getUserId());
        res.setEmployeeCode(user.getEmployeeCode());
        res.setFullName(user.getFullName());
        res.setEmail(user.getEmail());
        res.setAvatarUrl(user.getAvatarUrl());
        res.setPhoneNumber(user.getPhoneNumber());
        res.setAddress(user.getAddress());
        res.setGender(user.getGender());
        res.setDateOfBirth(user.getDateOfBirth());
        res.setIsActive(user.getIsActive());
        res.setCreatedAt(user.getCreatedAt());
        res.setCreatedBy(user.getCreatedBy());

        if (user.getRole() != null) {
            res.setRole(new ResUserCreateDTO.RoleUser(user.getRole().getRoleId(), user.getRole().getRoleName()));
        }
        return res;
    }

    public ResUserDTO userToResUserDTO(User user) {
        if (user == null)
            return null;
        ResUserDTO res = new ResUserDTO();
        res.setUserId(user.getUserId());
        res.setEmployeeCode(user.getEmployeeCode());
        res.setFullName(user.getFullName());
        res.setEmail(user.getEmail());
        res.setAvatarUrl(user.getAvatarUrl());
        res.setPhoneNumber(user.getPhoneNumber());
        res.setAddress(user.getAddress());
        res.setGender(user.getGender());
        res.setDateOfBirth(user.getDateOfBirth());
        res.setIsActive(user.getIsActive());
        res.setIsSystem(user.getIsSystem());
        res.setCreatedAt(user.getCreatedAt());
        res.setCreatedBy(user.getCreatedBy());
        res.setUpdatedAt(user.getUpdatedAt());
        res.setUpdatedBy(user.getUpdatedBy());

        if (user.getRole() != null) {
            res.setRole(new ResUserDTO.RoleUser(user.getRole().getRoleId(), user.getRole().getRoleName()));
        }
        return res;
    }

    public ReqUserUpdateDTO userToReqUserUpdate(User user) {
        if (user == null)
            return null;
        ReqUserUpdateDTO req = new ReqUserUpdateDTO();
        req.setFullName(user.getFullName());
        req.setEmail(user.getEmail());
        req.setAvatarUrl(user.getAvatarUrl());
        req.setPhoneNumber(user.getPhoneNumber());
        req.setAddress(user.getAddress());
        req.setGender(user.getGender());
        req.setDateOfBirth(user.getDateOfBirth());
        req.setRoleId(user.getRoleId());
        req.setIsActive(user.getIsActive());
        return req;
    }
}