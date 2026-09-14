package IVS.CMS.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

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
            throw new UsernameNotFoundException("User/Password không hợp lệ");
        }
        return new User(user.getEmail(), user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

    }
}
