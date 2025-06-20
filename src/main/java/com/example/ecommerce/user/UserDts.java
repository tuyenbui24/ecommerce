package com.example.ecommerce.user;

import com.example.ecommerce.role.entity.Role;
import com.example.ecommerce.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class UserDts implements UserDetails {

    private final User user;

    public UserDts(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = user.getRoles();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role r : roles) {
            authorities.add(new SimpleGrantedAuthority(r.getName()));
        }
        return authorities;
    }

    @Override public String getPassword() { return user.getPassword(); }

    @Override public String getUsername() { return user.getEmail(); }

    @Override public boolean isAccountNonExpired() { return true; }

    @Override public boolean isAccountNonLocked() { return true; }

    @Override public boolean isCredentialsNonExpired() { return true; }

    @Override public boolean isEnabled() {return true;}

    public String getFullName() {
        return user.getFirstName() + " " + user.getLastName();
    }

    public Integer getId() {
        return user.getId();
    }

    public User getUserEntity() {
        return user;
    }
}
