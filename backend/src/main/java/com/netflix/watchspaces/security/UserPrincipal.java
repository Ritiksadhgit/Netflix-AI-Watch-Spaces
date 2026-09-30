package com.netflix.watchspaces.security;

import com.netflix.watchspaces.domain.entity.User;
import com.netflix.watchspaces.domain.enums.RoleType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String displayName;
    private final RoleType role;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String email, String password, String displayName, RoleType role, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.displayName = displayName;
        this.role = role;
        this.authorities = authorities;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;
        private String password;
        private String displayName;
        private RoleType role;
        private Collection<? extends GrantedAuthority> authorities;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        public Builder role(RoleType role) { this.role = role; return this; }
        public Builder authorities(Collection<? extends GrantedAuthority> authorities) { this.authorities = authorities; return this; }

        public UserPrincipal build() {
            return new UserPrincipal(id, email, password, displayName, role, authorities);
        }
    }

    public static UserPrincipal create(User user) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        return UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .authorities(Collections.singletonList(authority))
                .build();
    }

    public boolean hasRole(String roleName) {
        String target = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return authorities != null && authorities.stream().anyMatch(a -> a.getAuthority().equalsIgnoreCase(target));
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public RoleType getRole() { return role; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
