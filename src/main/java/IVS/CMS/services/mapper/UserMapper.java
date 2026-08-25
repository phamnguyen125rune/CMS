package IVS.CMS.services.mapper;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.stereotype.Component;

import IVS.CMS.domain.Role;
import IVS.CMS.domain.User;
import IVS.CMS.domain.dto.request.ReqUserCreateDTO;
import IVS.CMS.domain.dto.request.ReqUserUpdateDTO;
import IVS.CMS.domain.dto.response.ResUserCreateDTO;
import IVS.CMS.domain.dto.response.ResUserDTO;

@Component
public class UserMapper {
    public User reqCreateToUser(ReqUserCreateDTO dto) {
        if (dto == null)
            return null;

        User user = new User();
        user.setFullname(dto.getFullname());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setPhone(dto.getPhone());
        user.setAge(dto.getAge());
        user.setAddress(dto.getAddress());
        user.setGender(dto.getGender());
        user.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getRoleId() != null) {
            Role role = new Role();
            role.setId(dto.getRoleId());
            user.setRole(role);
        }

        return user;
    }

    public ResUserCreateDTO userToResCreateDTO(User user) {
        if (user == null)
            return null;

        ResUserCreateDTO res = new ResUserCreateDTO();
        res.setId(user.getId());
        res.setFullname(user.getFullname());
        res.setEmail(user.getEmail());
        res.setAvatarUrl(user.getAvatarUrl());
        res.setPhone(user.getPhone());
        res.setAge(resolveAge(user));
        res.setAddress(user.getAddress());
        res.setGender(user.getGender());
        res.setEmployeeCode(user.getEmployeeCode());
        res.setDateOfBirth(user.getDateOfBirth());
        res.setStatus(user.getStatus());
        res.setCreatedAt(user.getCreatedAt());
        res.setCreatedBy(user.getCreatedBy());
        res.setUpdatedAt(user.getUpdatedAt());
        res.setUpdatedBy(user.getUpdatedBy());

        if (user.getRole() != null) {
            res.setRole(new ResUserCreateDTO.RoleUser(user.getRole().getId(), user.getRole().getName()));
        }

        return res;
    }

    public ResUserDTO userToResUserDTO(User user) {
        if (user == null)
            return null;

        ResUserDTO res = new ResUserDTO();
        res.setId(user.getId());
        res.setFullname(user.getFullname());
        res.setEmail(user.getEmail());
        res.setAvatarUrl(user.getAvatarUrl());
        res.setPhone(user.getPhone());
        res.setAge(resolveAge(user));
        res.setAddress(user.getAddress());
        res.setGender(user.getGender());
        res.setEmployeeCode(user.getEmployeeCode());
        res.setDateOfBirth(user.getDateOfBirth());
        res.setStatus(user.getStatus());
        res.setCreatedAt(user.getCreatedAt());
        res.setCreatedBy(user.getCreatedBy());
        res.setUpdatedAt(user.getUpdatedAt());
        res.setUpdatedBy(user.getUpdatedBy());
        res.setDeletedAt(user.getDeletedAt());
        res.setDeletedBy(user.getDeletedBy());

        if (user.getRole() != null) {
            res.setRole(new ResUserDTO.RoleUser(user.getRole().getId(), user.getRole().getName()));
        }

        return res;
    }

    public User resUserDTOToUser(ResUserDTO res) {
        if (res == null)
            return null;

        User user = new User();
        user.setId(res.getId());
        user.setFullname(res.getFullname());
        user.setEmail(res.getEmail());
        user.setAvatarUrl(res.getAvatarUrl());
        user.setPhone(res.getPhone());
        user.setAge(res.getAge());
        user.setAddress(res.getAddress());
        user.setGender(res.getGender());
        user.setEmployeeCode(res.getEmployeeCode());
        user.setDateOfBirth(res.getDateOfBirth());
        user.setStatus(res.getStatus());
        user.setCreatedAt(res.getCreatedAt());
        user.setCreatedBy(res.getCreatedBy());
        return user;
    }

    public ReqUserUpdateDTO userToReqUserUpdate(User user) {
        if (user == null)
            return null;

        ReqUserUpdateDTO req = new ReqUserUpdateDTO();
        req.setFullname(user.getFullname());
        req.setEmail(user.getEmail());
        req.setAvatarUrl(user.getAvatarUrl());
        req.setPhone(user.getPhone());
        req.setAge(resolveAge(user));
        req.setAddress(user.getAddress());
        req.setGender(user.getGender());
        req.setDateOfBirth(user.getDateOfBirth());
        return req;
    }

    public User reqUpdateToUser(ReqUserUpdateDTO dto) {
        if (dto == null)
            return null;

        User user = new User();
        user.setFullname(dto.getFullname());
        user.setEmail(dto.getEmail());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setPhone(dto.getPhone());
        user.setAge(dto.getAge());
        user.setAddress(dto.getAddress());
        user.setGender(dto.getGender());
        user.setDateOfBirth(dto.getDateOfBirth());

        return user;
    }

    private int resolveAge(User user) {
        if (user.getDateOfBirth() == null) {
            return user.getAge();
        }

        LocalDate today = LocalDate.now();
        if (user.getDateOfBirth().isAfter(today)) {
            return 0;
        }

        return Period.between(user.getDateOfBirth(), today).getYears();
    }
}
