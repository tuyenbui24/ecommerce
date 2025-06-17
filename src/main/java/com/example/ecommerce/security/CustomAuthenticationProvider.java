package com.example.ecommerce.security;

import com.example.ecommerce.staff.StaffDts;
import com.example.ecommerce.staff.StaffDtsService;
import com.example.ecommerce.user.UserDts;
import com.example.ecommerce.user.UserDtsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDtsService userDetailsService;
    private final StaffDtsService staffDetailsService;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationProvider(UserDtsService userDetailsService,
                                        StaffDtsService staffDetailsService,
                                        PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.staffDetailsService = staffDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        try {
            UserDts user = (UserDts) userDetailsService.loadUserByUsername(email);
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                return new UsernamePasswordAuthenticationToken(user, rawPassword, user.getAuthorities());
            }
        } catch (UsernameNotFoundException ignored) {}

        try {
            StaffDts staff = (StaffDts) staffDetailsService.loadUserByUsername(email);
            if (passwordEncoder.matches(rawPassword, staff.getPassword())) {
                return new UsernamePasswordAuthenticationToken(staff, rawPassword, staff.getAuthorities());
            }
        } catch (UsernameNotFoundException ignored) {}

        throw new BadCredentialsException("Thông tin đăng nhập không chính xác");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.equals(authentication);
    }
}

