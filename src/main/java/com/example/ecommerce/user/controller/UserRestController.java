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

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody UserRegisterRequest request) {
        userService.register(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(userService.listByPageU(page, keyword).getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.findDtoById(id));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailUnique(
            @RequestParam Integer id,
            @RequestParam String email) {
        boolean isUnique = userService.emailUnique(id, email);
        return ResponseEntity.ok(isUnique);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(
            @PathVariable Integer id,
            @RequestBody UserDTO userDTO,
            @RequestParam(required = false) String newPassword) {
        userDTO.setId(id);
        userService.updateUserInfo(userDTO, newPassword);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
