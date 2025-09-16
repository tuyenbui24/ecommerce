package com.example.ecommerce.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileUpdateRequest {

    @Email(message = "Email không hợp lệ")
    private String email;

    private String firstName;

    private String lastName;
}
