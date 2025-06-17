package com.example.ecommerce.staff.service;

import com.example.ecommerce.config.exception.UserNotFoundExp;
import com.example.ecommerce.role.entity.Role;
import com.example.ecommerce.role.repository.RoleRepository;
import com.example.ecommerce.staff.dto.StaffDTO;
import com.example.ecommerce.staff.entity.Staff;
import com.example.ecommerce.staff.mapper.StaffMapper;
import com.example.ecommerce.staff.repository.StaffRepository;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffService {

    public static final int STAFFS_IN_PAGE = 5;

    private final StaffRepository staffRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    public StaffService(StaffRepository staffRepo, RoleRepository roleRepo, PasswordEncoder encoder) {
        this.staffRepo = staffRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
    }

    public List<StaffDTO> listStaffDTOByPage(int pageNum, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, STAFFS_IN_PAGE, Sort.by("lastName").ascending());
        Page<Staff> staffPage = (keyword == null || keyword.isBlank())
                ? staffRepo.findAll(pageable)
                : staffRepo.searchS(keyword, pageable);

        return staffPage.getContent().stream()
                .map(StaffMapper::toDTO)
                .toList();
    }

    public List<Role> findAllRoles() {
        return roleRepo.findAll();
    }

    public void encryptPassword(Staff staff) {
        staff.setPassword(encoder.encode(staff.getPassword()));
    }

    public Staff save(Staff staff) {
        boolean staffUpdate = staff.getId() != null;

        if (staffUpdate) {
            Staff existing = staffRepo.findById(staff.getId()).orElse(null);
            if (existing != null) {
                if (staff.getPassword().isBlank()) {
                    staff.setPassword(existing.getPassword());
                } else {
                    encryptPassword(staff);
                }
            } else {
                throw new IllegalArgumentException("Staff not found with ID: " + staff.getId());
            }
        } else {
            encryptPassword(staff);
        }
        return staffRepo.save(staff);
    }

    public Staff getId(Integer id) throws UserNotFoundExp {
        return staffRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundExp("Could not find any staff with id: " + id));
    }

    public void delete(Integer id) throws UserNotFoundExp {
        Staff staff = staffRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundExp("Could not find any staff with ID " + id));
        staffRepo.delete(staff);
    }

    public void updateStatus(Integer id, boolean enabled) {
        staffRepo.updateEnabled(id, enabled);
    }

    public boolean isEmailUnique(Integer id, String email) {
        Staff staffByEmail = staffRepo.getStaffByEmail(email);
        return staffByEmail == null || staffByEmail.getId().equals(id);
    }
}
