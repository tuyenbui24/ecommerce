package com.example.ecommerce.security;

import com.example.ecommerce.user.UserDts;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static Integer getCurrentUserId() {
        var principal = (UserDts) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getId();
    }
}
