package IVS.CMS.services;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import IVS.CMS.domain.Permission;
import IVS.CMS.domain.Role;
import IVS.CMS.domain.User;
import IVS.CMS.repositories.RoleRepository;
import IVS.CMS.repositories.UserRepository;

@Service
public class PermissionCacheService {
    private static final long DEFAULT_TTL_SECONDS = 3600L;

    private final ConcurrentMap<Long, CacheEntry> authorityCache = new ConcurrentHashMap<>();
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final long ttlSeconds;

    public PermissionCacheService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            @Value("${CMS.permission-cache.ttl-seconds:3600}") long ttlSeconds) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.ttlSeconds = ttlSeconds > 0 ? ttlSeconds : DEFAULT_TTL_SECONDS;
    }

    public Set<String> getAuthorities(long userId) {
        Instant now = Instant.now();
        Duration ttl = cacheTtl();

        CacheEntry entry = authorityCache.compute(userId, (key, current) -> {
            if (current != null && !current.isExpired(now)) {
                current.refresh(now, ttl);
                return current;
            }
            return CacheEntry.of(loadAuthoritiesFromDatabase(key), now, ttl);
        });

        return entry != null ? entry.authorities() : Collections.emptySet();
    }

    public Set<String> cacheUser(User user) {
        if (user == null || user.getId() <= 0) {
            return Collections.emptySet();
        }

        Set<String> authorities = buildAuthorities(user);
        authorityCache.put(user.getId(), CacheEntry.of(authorities, Instant.now(), cacheTtl()));
        return authorities;
    }

    public void evictUser(long userId) {
        authorityCache.remove(userId);
    }

    public void evictUsersByRoleId(long roleId) {
        List<User> users = this.userRepository.findByRoleId(roleId);
        users.forEach(user -> evictUser(user.getId()));
    }

    public void evictAll() {
        authorityCache.clear();
    }

    @Scheduled(fixedDelayString = "${CMS.permission-cache.cleanup-interval-ms:300000}")
    public void evictExpiredEntries() {
        Instant now = Instant.now();
        authorityCache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    public List<String> getPermissionCodes(User user) {
        return buildAuthorities(user).stream()
                .filter(authority -> !authority.startsWith("ROLE_"))
                .sorted()
                .collect(Collectors.toList());
    }

    private Set<String> loadAuthoritiesFromDatabase(long userId) {
        return this.userRepository.findById(userId)
                .map(this::loadFullRoleIfNeeded)
                .map(this::buildAuthorities)
                .orElseGet(Collections::emptySet);
    }

    private User loadFullRoleIfNeeded(User user) {
        if (user.getRole() != null && user.getRole().getId() > 0) {
            Role fullRole = this.roleRepository.findById(user.getRole().getId()).orElse(null);
            user.setRole(fullRole);
        }
        return user;
    }

    private Set<String> buildAuthorities(User user) {
        if (!isUsableUser(user)) {
            return Collections.emptySet();
        }

        Set<String> authorities = new HashSet<>();
        Role role = user.getRole();
        if (role != null && role.getName() != null && role.isActive()) {
            String roleName = role.getName().trim().toUpperCase();
            authorities.add("ROLE_" + roleName);
            authorities.addAll(buildPermissionCodes(user));
            if ("SUPER_ADMIN".equals(roleName) || "ADMIN".equals(roleName)) {
                authorities.addAll(defaultAdminAuthorities());
            } else if ("USER".equals(roleName) || "NORMAL_USER".equals(roleName)) {
                authorities.addAll(defaultUserAuthorities());
            }
        }
        return Collections.unmodifiableSet(authorities);
    }

    private Set<String> buildPermissionCodes(User user) {
        Role role = user != null ? user.getRole() : null;
        if (role == null || role.getPermissions() == null) {
            return Collections.emptySet();
        }
        return role.getPermissions().stream()
                .map(this::toPermissionCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private String toPermissionCode(Permission permission) {
        if (permission == null) {
            return null;
        }
        permission.normalizePermissionCode();
        return permission.getPermissionCode();
    }

    private boolean isUsableUser(User user) {
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            return false;
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            return false;
        }
        return user.getStatus() == null || !"LOCKED".equalsIgnoreCase(user.getStatus());
    }

    private Duration cacheTtl() {
        return Duration.ofSeconds(ttlSeconds);
    }

    private Set<String> defaultAdminAuthorities() {
        return Set.of(
                "auth:EDIT",
                "profile:VIEW",
                "profile:EDIT",
                "users:VIEW",
                "users:EDIT",
                "roles:VIEW",
                "roles:EDIT",
                "permissions:VIEW",
                "permissions:EDIT",
                "contacts:VIEW",
                "contacts:EDIT");
    }

    private Set<String> defaultUserAuthorities() {
        return Set.of(
                "auth:EDIT",
                "profile:VIEW",
                "profile:EDIT",
                "users:VIEW");
    }

    private static final class CacheEntry {
        private final Set<String> authorities;
        private volatile Instant expiresAt;

        private CacheEntry(Set<String> authorities, Instant expiresAt) {
            this.authorities = authorities != null ? Collections.unmodifiableSet(new HashSet<>(authorities))
                    : Collections.emptySet();
            this.expiresAt = expiresAt;
        }

        static CacheEntry of(Set<String> authorities, Instant now, Duration ttl) {
            return new CacheEntry(authorities, now.plus(ttl));
        }

        Set<String> authorities() {
            return authorities;
        }

        boolean isExpired(Instant now) {
            return !expiresAt.isAfter(now);
        }

        void refresh(Instant now, Duration ttl) {
            this.expiresAt = now.plus(ttl);
        }
    }
}
