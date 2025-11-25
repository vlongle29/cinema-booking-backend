package com.example.CineBook.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

/**
 * Custom UserDetails implementation to support getUserId()
 */
public record CustomUserDetails(UUID userId, String username, String password,
                                Collection<? extends GrantedAuthority> authorities, boolean enabled,
                                boolean accountNonExpired, boolean accountNonLocked,
                                boolean credentialsNonExpired) implements UserDetails {

    public CustomUserDetails(UUID userId, String username, String password,
                             Collection<? extends GrantedAuthority> authorities) {
        this(userId, username, password, authorities, true, true, true, true);
    }

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
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
