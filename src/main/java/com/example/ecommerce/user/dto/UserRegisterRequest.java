package com.example.ecommerce.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRegisterRequest {
    @Email @NotBlank
    private String email;

    @Size(min = 6) @NotBlank
    private String password;

    @NotBlank private String firstName;
    @NotBlank private String lastName;
}
