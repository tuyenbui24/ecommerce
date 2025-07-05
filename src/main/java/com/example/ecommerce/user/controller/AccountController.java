package com.example.ecommerce.user.controller;

import com.example.ecommerce.security.SecurityUtils;
import com.example.ecommerce.user.dto.UserDTO;
import com.example.ecommerce.user.entity.User;
import com.example.ecommerce.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    public AccountController(UserService userService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public String viewAccountInfo(Model model) {
        Integer userId = securityUtils.getCurrentUserId();
        UserDTO user = userService.findDtoById(userId);
        model.addAttribute("user", user);
        return "account_info";
    }

    @PostMapping("/update")
    public String updateAccount(@ModelAttribute("user") UserDTO dto,
                                @RequestParam(required = false) String oldPassword,
                                @RequestParam(required = false) String newPassword,
                                @RequestParam(required = false) String confirmPassword,
                                RedirectAttributes ra) {
        Integer userId = securityUtils.getCurrentUserId();
        User user = userService.getById(userId);

        if (!user.getId().equals(dto.getId())) {
            ra.addFlashAttribute("error", "Không hợp lệ!");
            return "redirect:/account";
        }

        if ((newPassword != null && !newPassword.isBlank()) ||
                (confirmPassword != null && !confirmPassword.isBlank())) {

            if (oldPassword == null || oldPassword.isBlank()) {
                ra.addFlashAttribute("error", "Vui lòng nhập mật khẩu hiện tại để đổi mật khẩu!");
                return "redirect:/account";
            }

            if (!userService.checkPassword(user, oldPassword)) {
                ra.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
                return "redirect:/account";
            }

            if (!newPassword.equals(confirmPassword)) {
                ra.addFlashAttribute("error", "Mật khẩu mới và xác nhận không trùng khớp!");
                return "redirect:/account";
            }
        }
        userService.updateUserInfo(dto, newPassword);
        ra.addFlashAttribute("message", "✅ Cập nhật thành công");
        return "redirect:/account";
    }
}

