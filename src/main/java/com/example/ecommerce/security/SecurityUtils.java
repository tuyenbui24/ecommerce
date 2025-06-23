package com.example.ecommerce.security;

import com.example.ecommerce.staff.StaffDts;
import com.example.ecommerce.user.UserDts;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    public Integer getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDts user) {
            return user.getId();
        }

        if (principal instanceof StaffDts staff) {
            throw new RuntimeException("Không được vào!");
        }
        throw new RuntimeException("Không thể xác định kiểu người dùng.");
    }
}
