package com.example.ecommerce.staff.service;

import com.example.ecommerce.config.exception.UserNotFoundExp;
import com.example.ecommerce.role.entity.Role;
import com.example.ecommerce.role.repository.RoleRepository;
import com.example.ecommerce.staff.dto.StaffCreateRequest;
import com.example.ecommerce.staff.dto.StaffDTO;
import com.example.ecommerce.staff.entity.Staff;
import com.example.ecommerce.staff.mapper.StaffMapper;
import com.example.ecommerce.staff.repository.StaffRepository;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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

    public Staff save(StaffCreateRequest request) {
        boolean staffUpdate = request.getId() != null;
        Staff staff;

        if (staffUpdate) {
            staff = staffRepo.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Staff not found with ID: " + request.getId()));
        } else {
            staff = new Staff();
        }

        staff.setEmail(request.getEmail());
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setEnabled(request.isEnabled());
        staff.setPhotos(request.getPhotos());

        if (!staffUpdate || (request.getPassword() != null && !request.getPassword().isBlank())) {
            staff.setPassword(encoder.encode(request.getPassword()));
        }

        List<Role> roles = roleRepo.findAllById(request.getRoleIds());
        staff.setRoles(Set.copyOf(roles));

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
