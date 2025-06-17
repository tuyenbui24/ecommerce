package com.example.ecommerce.staff.dto;

import com.example.ecommerce.role.dto.RoleDTO;
import lombok.Data;

import java.util.List;

@Data
public class StaffDTO {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String photoPath;
    private boolean enabled;
    private List<RoleDTO> roles;
}
