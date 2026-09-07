package IVS.CMS.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import IVS.CMS.domain.User;

@Repository
public interface UserRepository {

    User save(User user);

    Optional<User> findById(long userId);

    List<User> findAll(int limit, int offset);

    User findByEmail(String email);

    boolean existsByEmail(String email);

    int softDelete(long userId, long currentUserId, LocalDateTime deletedAt);

    int hardDelete(long userId, long currentUserId);

    void restore(long userId, long currentUserId, LocalDateTime updatedAt);

    boolean existsByEmailForUpdate(long userId, String email);

    boolean existsByPhoneNumber(String phoneNumber);


    Optional<User> findByIdIncludeDeleted(long userId);

    long count();

    String findMaxEmployeeCode();

    List<User> findByRoleId(long roleId);

    long countByRoleId(long roleId);

    List<User> findByRoleName(String roleName);

    List<User> findDeletedUsers();

    void updateStatus(long userId, boolean isActive, Long updatedBy, LocalDateTime updatedAt);

    void updatePassword(long userId, String passwordHash, Long updatedBy, LocalDateTime updatedAt);

    User findByEmployeeCode(String employeeCode);

    User findByEmailOrEmployeeCode(String loginId);

    void updateLoginSecurity(long userId, int failedAttempts, int lockCount, LocalDateTime lockedUntil);

    void clearLoginFailures(long userId);

    User findByEmailOrEmployeeCodeIncludeDeleted(String loginId);

}