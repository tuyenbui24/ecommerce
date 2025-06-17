package com.example.ecommerce.user.dto;

import com.example.ecommerce.role.dto.RoleDTO;
import lombok.Data;
import java.util.List;

@Data
public class UserDTO {
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private List<RoleDTO> roles;
}
