package vn.edu.iuh.security;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.edu.iuh.models.User;
import vn.edu.iuh.models.enums.UserStatus;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserPrincipal implements UserDetails {
    private String id;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private UserStatus status;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.username = user.getPhone();
        this.password = user.getPassword();
        this.authorities = List.of(new SimpleGrantedAuthority(user.getRole().name()));
        this.status = user.getStatus();
    }

    @Override
    public boolean isAccountNonExpired() {
        return status.equals(UserStatus.ACTIVE);
    }

    @Override
    public boolean isAccountNonLocked() {
        return !status.equals(UserStatus.LOCKED);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return status.equals(UserStatus.ACTIVE);
    }

    @Override
    public boolean isEnabled() {
        return status.equals(UserStatus.ACTIVE);
    }
}
