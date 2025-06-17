package com.example.ecommerce.user.mapper;

import com.example.ecommerce.role.mapper.RoleMapper;
import com.example.ecommerce.user.dto.UserDTO;
import com.example.ecommerce.user.entity.User;

public class UserMapper {
    public static UserDTO toDTO(User u){
        if(u == null) return null;
        UserDTO d = new UserDTO();
        d.setId(u.getId());
        d.setEmail(u.getEmail());
        d.setFirstName(u.getFirstName());
        d.setLastName(u.getLastName());
        d.setFullName(u.getFullName());
        d.setRoles(u.getRoles().stream()
                .map(RoleMapper::toDTO)
                .toList());
        return d;
    }
}
