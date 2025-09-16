package com.example.ecommerce.user.controller;

import com.example.ecommerce.user.dto.ChangePasswordRequest;
import com.example.ecommerce.user.dto.UserDTO;
import com.example.ecommerce.user.dto.UserProfileUpdateRequest;
import com.example.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// UserMeRestController
@RestController
@RequestMapping("/api/users/me")
public class UserMeRestController {

    private final UserService userService;

    public UserMeRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        Integer userId = userService.findIdByUsername(username);
        return ResponseEntity.ok(userService.findDtoById(userId));
    }

    @PutMapping
    public ResponseEntity<Void> updateProfile(Authentication authentication,
                                              @Valid @RequestBody UserProfileUpdateRequest request) {
        String username = authentication.getName();
        Integer userId = userService.findIdByUsername(username);
        userService.updateUserProfile(userId, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    public ResponseEntity<String> changePassword(Authentication authentication,
                                                 @Valid @RequestBody ChangePasswordRequest request) {
        String username = authentication.getName();
        Integer userId = userService.findIdByUsername(username);

        var user = userService.getById(userId);
        if (!userService.checkPassword(user, request.getOldPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu hiện tại không đúng!");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu mới và xác nhận không trùng khớp!");
        }
        userService.updatePassword(userId, request.getNewPassword());
        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }
}

