package com.example.ecommerce.staff;

import com.example.ecommerce.role.entity.Role;
import com.example.ecommerce.staff.entity.Staff;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class StaffDts implements UserDetails {

    private final Staff staff;

    public StaffDts(Staff staff) {
        this.staff = staff;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = staff.getRoles();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role r : roles) {
            authorities.add(new SimpleGrantedAuthority(r.getName()));
        }
        return authorities;
    }

    @Override public String getPassword() { return staff.getPassword(); }

    @Override public String getUsername() { return staff.getEmail(); }

    @Override public boolean isAccountNonExpired() { return true; }

    @Override public boolean isAccountNonLocked() { return true; }

    @Override public boolean isCredentialsNonExpired() { return true; }

    @Override public boolean isEnabled() {
        return staff.isEnabled();
    }

    public String getFullName() {
        return staff.getFirstName() + " " + staff.getLastName();
    }

    public Staff getStaffEntity() {
        return staff;
    }
}
