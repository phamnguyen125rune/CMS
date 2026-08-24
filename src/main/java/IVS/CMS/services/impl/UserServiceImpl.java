package IVS.CMS.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import IVS.CMS.domain.User;
import IVS.CMS.repositories.RefreshTokenRepository;
import IVS.CMS.repositories.UserRepository;
import IVS.CMS.security.SecurityService;
import IVS.CMS.services.UserService;
import IVS.CMS.services.dto.request.ReqChangePasswordDTO;
import IVS.CMS.services.dto.request.ReqUserCreateDTO;
import IVS.CMS.services.dto.request.ReqUserUpdateDTO;
import IVS.CMS.services.dto.response.ResUserCreateDTO;
import IVS.CMS.services.dto.response.ResUserDTO;
import IVS.CMS.services.dto.response.ResultPaginationDTO;
import IVS.CMS.services.error.BadRequestException;
import IVS.CMS.services.error.ResourceNotFoundException;
import IVS.CMS.services.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public synchronized ResUserCreateDTO createUser(ReqUserCreateDTO req) {
        if (this.userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email '" + req.getEmail() + "' đã tồn tại!");
        }
        if (req.getPhoneNumber() != null && !req.getPhoneNumber().isBlank()
                && this.userRepository.existsByPhoneNumber(req.getPhoneNumber())) {
            throw new BadRequestException("Số điện thoại '" + req.getPhoneNumber() + "' đã tồn tại!");
        }

        User user = userMapper.reqCreateToUser(req);
        user.setIsActive(true);
        user.setIsSystem(false);
        user.setFailedLoginAttempts(0);
        user.setLockCount(0);
        user.setEmployeeCode(generateEmployeeCode());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy(SecurityService.getCurrentUserId().orElse(null));

        User savedUser = this.userRepository.save(user);
        return this.userMapper.userToResCreateDTO(savedUser);
    }

    @Override
    public User fetchUserById(long userId) {
        return this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User với id " + userId + " không tồn tại"));
    }

    @Override
    public ResUserDTO getUserById(long userId) {
        User user = this.fetchUserById(userId);
        return userMapper.userToResUserDTO(user);
    }

    @Override
    public ResultPaginationDTO findAll(int page, int pageSize) {
        if (page < 1)
            page = 1;
        if (pageSize < 1)
            pageSize = 10;

        long total = this.userRepository.count();
        int pages = (int) Math.ceil((double) total / pageSize);
        int offset = (page - 1) * pageSize;

        List<User> users = this.userRepository.findAll(pageSize, offset);
        List<ResUserDTO> listUserRes = users.stream()
                .map(userMapper::userToResUserDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(page);
        meta.setPageSize(pageSize);
        meta.setPages(pages);
        meta.setTotal(total);

        ResultPaginationDTO res = new ResultPaginationDTO();
        res.setMeta(meta);
        res.setResult(listUserRes);
        return res;
    }

    @Override
    @Transactional
    public ResUserDTO updateUser(long userId, ReqUserUpdateDTO req) {
        User userCurrent = this.fetchUserById(userId);

        if (Boolean.TRUE.equals(userCurrent.getIsSystem())) {
            throw new BadRequestException("Không thể cập nhật tài khoản hệ thống");
        }

        if (!userCurrent.getEmail().equalsIgnoreCase(req.getEmail())
                && this.userRepository.existsByEmailForUpdate(userId, req.getEmail())) {
            throw new BadRequestException("Email '" + req.getEmail() + "' đã được sử dụng");
        }

        if (req.getPhoneNumber() != null && !req.getPhoneNumber().isBlank()
                && this.userRepository.existsByPhoneNumberForUpdate(userId, req.getPhoneNumber())) {
            throw new BadRequestException("Số điện thoại '" + req.getPhoneNumber() + "' đã được sử dụng");
        }

        userCurrent.setFullName(req.getFullName());
        userCurrent.setEmail(req.getEmail());
        userCurrent.setPhoneNumber(req.getPhoneNumber());
        userCurrent.setAddress(req.getAddress());
        userCurrent.setAvatarUrl(req.getAvatarUrl());
        userCurrent.setGender(req.getGender());
        userCurrent.setDateOfBirth(req.getDateOfBirth());
        userCurrent.setRoleId(req.getRoleId());

        if (req.getIsActive() != null) {
            userCurrent.setIsActive(req.getIsActive());
        }

        userCurrent.setUpdatedAt(LocalDateTime.now());
        userCurrent.setUpdatedBy(SecurityService.getCurrentUserId().orElse(null));

        User updatedUser = this.userRepository.save(userCurrent);
        return this.userMapper.userToResUserDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        User user = this.fetchUserById(userId);

        if (Boolean.TRUE.equals(user.getIsSystem())) {
            throw new BadRequestException("Không thể xóa tài khoản hệ thống");
        }

        long currentUserId = SecurityService.getCurrentUserId().orElse(0L);
        if (userId == currentUserId) {
            throw new BadRequestException("Không thể tự xóa tài khoản của chính mình");
        }

        this.userRepository.softDelete(userId, currentUserId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void updateStatus(long userId, Boolean isActive) {
        if (isActive == null) {
            throw new BadRequestException("Trường 'isActive' không được để trống");
        }

        User user = this.fetchUserById(userId);
        if (Boolean.TRUE.equals(user.getIsSystem())) {
            throw new BadRequestException("Không thể thay đổi trạng thái tài khoản hệ thống");
        }

        long currentUserId = SecurityService.getCurrentUserId().orElse(0L);
        this.userRepository.updateStatus(userId, isActive, currentUserId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void changePassword(ReqChangePasswordDTO req) {
        long currentUserId = SecurityService.getCurrentUserId()
                .orElseThrow(() -> new BadRequestException("Không xác định được user hiện tại"));

        User user = this.fetchUserById(currentUserId);

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Mật khẩu cũ không chính xác");
        }

        String newHash = passwordEncoder.encode(req.getNewPassword());
        this.userRepository.updatePassword(currentUserId, newHash, currentUserId, LocalDateTime.now());
        this.refreshTokenRepository.deleteByUserId(currentUserId);
    }

    @Override
    public User handleGetUserByEmailOrEmployeeCode(String loginId) {
        return this.userRepository.findByEmailOrEmployeeCode(loginId);
    }

    @Override
    @Transactional
    public synchronized ResUserCreateDTO register(ReqUserCreateDTO req) {
        return createUser(req);
    }

    private String generateEmployeeCode() {
        String maxCode = this.userRepository.findMaxEmployeeCode();
        if (maxCode == null || maxCode.trim().isEmpty()) {
            return "EMP0001";
        }
        try {
            int currentNum = Integer.parseInt(maxCode.substring(3));
            return String.format("EMP%04d", currentNum + 1);
        } catch (Exception e) {
            return "EMP0001";
        }
    }
}