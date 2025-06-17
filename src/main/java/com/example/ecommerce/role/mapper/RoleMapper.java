package com.example.ecommerce.role.mapper;

import com.example.ecommerce.role.dto.RoleDTO;
import com.example.ecommerce.role.entity.Role;

public class RoleMapper {

    public static RoleDTO toDTO(Role role) {
        if (role == null) return null;

        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());

        return dto;
    }

    public static Role toEntity(RoleDTO dto) {
        if (dto == null) return null;

        Role role = new Role();
        role.setId(dto.getId());
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());

        return role;
    }
}
