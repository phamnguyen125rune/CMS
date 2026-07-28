package IVS.CMS.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import IVS.CMS.domain.Permission;
import IVS.CMS.domain.Role;
import IVS.CMS.services.UserService;

@Component("userDetailsService")
public class UserDetailsCustom implements UserDetailsService {
    private final UserService userService;

    public UserDetailsCustom(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        IVS.CMS.domain.User user = this.userService.handleGetUserByEmailOrEmployeeCode(username);

        if (user == null) {
            throw new UsernameNotFoundException("Username/Email không tồn tại");
        }

        boolean isAccountActive = !"LOCKED".equalsIgnoreCase(user.getStatus());
        Set<SimpleGrantedAuthority> authorities = buildAuthorities(user);

        return new User(
                user.getEmail(),
                user.getPassword(),
                isAccountActive,
                true,
                true,
                isAccountActive,
                authorities);
    }

    private Set<SimpleGrantedAuthority> buildAuthorities(IVS.CMS.domain.User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        Role role = user.getRole();

        if (role == null || role.getName() == null || !role.isActive()) {
            return authorities;
        }

        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().trim().toUpperCase()));

        if (role.getPermissions() != null) {
            for (Permission permission : role.getPermissions()) {
                permission.normalizePermissionCode();
                if (permission.getPermissionCode() != null && !permission.getPermissionCode().isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(permission.getPermissionCode()));
                }
            }
        }

        return authorities;
    }
}
