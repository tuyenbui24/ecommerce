package com.example.ecommerce.staff.mapper;

import com.example.ecommerce.role.entity.Role;
import com.example.ecommerce.role.mapper.RoleMapper;
import com.example.ecommerce.staff.dto.StaffCreateRequest;
import com.example.ecommerce.staff.dto.StaffDTO;
import com.example.ecommerce.staff.entity.Staff;

public class StaffMapper {

    public static StaffDTO toDTO(Staff staff) {
        if (staff == null) return null;

        StaffDTO dto = new StaffDTO();
        dto.setId(staff.getId());
        dto.setEmail(staff.getEmail());
        dto.setFirstName(staff.getFirstName());
        dto.setLastName(staff.getLastName());
        dto.setFullName(staff.getFullName());
        dto.setPhotoPath(staff.getPhotosImagePath());
        dto.setEnabled(staff.isEnabled());

        dto.setRoles(
                staff.getRoles().stream()
                        .map(RoleMapper::toDTO)
                        .toList()
        );

        return dto;
    }

    public static StaffCreateRequest toRequest(Staff staff) {
        if (staff == null) return null;

        StaffCreateRequest req = new StaffCreateRequest();
        req.setId(staff.getId());
        req.setEmail(staff.getEmail());
        req.setFirstName(staff.getFirstName());
        req.setLastName(staff.getLastName());
        req.setEnabled(staff.isEnabled());
        req.setPhotos(staff.getPhotos());
        req.setRoleIds(
                staff.getRoles().stream()
                        .map(Role::getId)
                        .toList()
        );
        return req;
    }
}

