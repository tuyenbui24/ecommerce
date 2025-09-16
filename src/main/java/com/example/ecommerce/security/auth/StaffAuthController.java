package com.example.ecommerce.security.auth;

import com.example.ecommerce.role.entity.Role;
import com.example.ecommerce.security.jwt.JwtUtil;
import com.example.ecommerce.staff.entity.Staff;
import com.example.ecommerce.staff.repository.StaffRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/staffs")
public class StaffAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final StaffRepository staffRepository;

    public StaffAuthController(AuthenticationManager authenticationManager,
                               JwtUtil jwtUtil,
                               StaffRepository staffRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.staffRepository = staffRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            Staff staff = staffRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Khng tìm thấy Nhân viên hoặc Quản lý"));

            String token = jwtUtil.generateToken(staff.getEmail());

            return ResponseEntity.ok(new JwtResponse(
                    token,
                    staff.getId(),
                    staff.getEmail(),
                    staff.getFirstName(),
                    staff.getLastName(),
                    staff.getRoles().stream()
                            .map(Role::getName)
                            .collect(Collectors.toSet())
            ));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Email hoặc mật khẩu không hợp lệ");
        }
    }
}
