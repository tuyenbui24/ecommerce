package com.example.ecommerce.user.controller;

import com.example.ecommerce.export.UserCsvExporter;
import com.example.ecommerce.user.dto.UserDTO;
import com.example.ecommerce.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;
    public UserRestController(UserService userService) { this.userService = userService; }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword) {

        Page<UserDTO> userPage = userService.listByPageU(page, keyword);

        Map<String, Object> res = new HashMap<>();
        res.put("data",        userPage.getContent());
        res.put("currentPage", page);
        res.put("totalItems",  userPage.getTotalElements());
        res.put("totalPages",  userPage.getTotalPages());

        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.findDtoById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export")
    public void exportUsers(
            @RequestParam(defaultValue = "") String keyword,
            HttpServletResponse response
    ) throws IOException {

        var users = userService.findForExport(keyword);
        UserCsvExporter exporter = new UserCsvExporter();
        exporter.export(users, response);
    }
}
