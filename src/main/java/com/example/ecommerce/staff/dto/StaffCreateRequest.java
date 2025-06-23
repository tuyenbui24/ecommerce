package com.example.ecommerce.staff.dto;

import lombok.Data;

import java.util.List;

@Data
public class StaffCreateRequest {
    private Integer id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private String photos;
    private List<Integer> roleIds;
}

