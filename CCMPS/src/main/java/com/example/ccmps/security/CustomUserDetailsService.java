package com.example.ccmps.security;

import com.example.ccmps.entity.AppUser;
import com.example.ccmps.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(
            String username)
            throws UsernameNotFoundException {

        AppUser appUser =
                appUserRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "User not found: "
                                                        + username
                                        )
                        );

        String roleAuthority =
                "ROLE_"
                        + appUser
                        .getRole()
                        .name();

        return User.builder()
                .username(
                        appUser.getUsername()
                )
                .password(
                        appUser.getPassword()
                )
                .disabled(
                        !Boolean.TRUE.equals(
                                appUser.getEnabled()
                        )
                )
                .authorities(
                        List.of(
                                new SimpleGrantedAuthority(
                                        roleAuthority
                                )
                        )
                )
                .build();
    }
}