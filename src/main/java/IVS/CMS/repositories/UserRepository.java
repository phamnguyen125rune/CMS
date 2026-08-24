package IVS.CMS.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import IVS.CMS.domain.User;

@Repository
public interface UserRepository {

    User save(User user);

    Optional<User> findById(long id);

    List<User> findAll(int limit, int offset);

    User findByEmail(String email);

    Optional<User> findByEmailIncludeDeleted(String email);

    boolean existsByEmail(String email);

    User findByRefreshTokenAndEmail(String refreshToken, String email);

    int softDelete(long id, long currentUserId, String deletedBy);

    int hardDelete(long id, long currentUserId);

    void restore(long id);

    Optional<User> findByIdIncludeDeleted(long id);

    long count();

    String findMaxEmployeeCode();

    List<User> findByRoleId(long roleId);

    long countByRoleId(long roleId);

    List<User> findByRoleName(String roleName);

    void updateUserRole(long userId, long roleId);

    List<User> findDeletedUsers();

    void updateStatus(long id, String status);

    void updateLoginSecurity(long id, int failedLoginAttempts, int lockCount, java.time.Instant lockedUntil);

    void clearLoginFailures(long id);

    void resetLoginSecurity(long id);

    User findByEmployeeCode(String employeeCode);

    User findByEmailOrEmployeeCode(String loginId);
}
