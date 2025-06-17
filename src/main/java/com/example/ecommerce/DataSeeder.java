package com.example.ecommerce;

import com.example.ecommerce.role.entity.Role;
import com.example.ecommerce.role.repository.RoleRepository;
import com.example.ecommerce.staff.entity.Staff;
import com.example.ecommerce.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepo;
    private final StaffRepository staffRepo;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) {

        if (roleRepo.count() == 0) {
            roleRepo.save(new Role("ROLE_ADMIN", "quản lý mọi thứ"));
            roleRepo.save(new Role("ROLE_USER",  "khách hàng"));
        }

        if (staffRepo.count() == 0) {
            Role roleAdmin = roleRepo.findByName("ROLE_ADMIN")
                    .orElseThrow();
            Staff staff = Staff.builder()
                    .email("quangtuyen10x@gmail.com")
                    .password(passwordEncoder.encode("buituyen10x"))
                    .firstName("Bùi")
                    .lastName("Tuyến")
                    .enabled(true)
                    .build();
            staff.addRole(roleAdmin);
            staffRepo.save(staff);
        }
    }
}
