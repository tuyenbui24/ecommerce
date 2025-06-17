package com.example.ecommerce.staff;

import com.example.ecommerce.staff.entity.Staff;
import com.example.ecommerce.staff.repository.StaffRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StaffDtsService implements UserDetailsService {

    private final StaffRepository staffRepo;

    public StaffDtsService(StaffRepository staffRepo) {
        this.staffRepo = staffRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Staff staff = staffRepo.getStaffByEmail(email);
        if (staff == null) {
            throw new UsernameNotFoundException("Không tìm thấy nhân viên: " + email);
        }
        return new StaffDts(staff);
    }
}