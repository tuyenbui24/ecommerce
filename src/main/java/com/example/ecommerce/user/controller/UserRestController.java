package com.example.ecommerce.user.controller;

import com.example.ecommerce.user.dto.UserDTO;
import com.example.ecommerce.user.dto.UserRegisterRequest;
import com.example.ecommerce.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    // Đăng ký người dùng mới
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody UserRegisterRequest request) {
        userService.register(request);
        return ResponseEntity.ok().build();
    }

    // Danh sách người dùng (phân trang và tìm kiếm)
    @GetMapping
    public ResponseEntity<List<UserDTO>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(userService.listByPageU(page, keyword).getContent());
    }

    // Lấy chi tiết người dùng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.findDtoById(id));
    }

    // Kiểm tra email có duy nhất không (dùng khi tạo/sửa)
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailUnique(
            @RequestParam Integer id,
            @RequestParam String email) {
        boolean isUnique = userService.emailUnique(id, email);
        return ResponseEntity.ok(isUnique);
    }

    // Cập nhật thông tin người dùng
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(
            @PathVariable Integer id,
            @RequestBody UserDTO userDTO,
            @RequestParam(required = false) String newPassword) {
        userDTO.setId(id);
        userService.updateUserInfo(userDTO, newPassword);
        return ResponseEntity.noContent().build();
    }

    // Xoá người dùng theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
