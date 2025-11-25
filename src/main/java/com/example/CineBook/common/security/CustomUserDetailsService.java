package com.example.CineBook.common.security;

import com.example.CineBook.dto.auth.AuthorityProjection;
import com.example.CineBook.model.SysUser;
import com.example.CineBook.repository.irepository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final SysUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Collection<GrantedAuthority> authorities = getAuthorities(user.getId());

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities,
                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isAccountNonLocked(),
                user.isCredentialsNonExpired()
        );
    }


    private Collection<GrantedAuthority> getAuthorities(java.util.UUID userId) {
        List<AuthorityProjection> projections = userRepository.findAllAuthoritiesByUserId(userId);
        Set<GrantedAuthority> authorities = new HashSet<>();

        for (AuthorityProjection projection : projections) {
            // Thêm role với prefix ROLE_
            if (projection.getRoleCode() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + projection.getRoleCode()));
            }
            // Thêm permission không có prefix
            if (projection.getPermissionCode() != null) {
                authorities.add(new SimpleGrantedAuthority(projection.getPermissionCode()));
            }
        }

        return authorities;
    }
}
