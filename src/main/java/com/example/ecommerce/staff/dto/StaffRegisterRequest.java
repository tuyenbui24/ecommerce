package com.example.ecommerce.staff.dto;

import lombok.Data;

@Data
public class StaffRegisterRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}
