
package com.example.ecommerce.user.controller;

import com.example.ecommerce.user.dto.UserDTO;
import com.example.ecommerce.user.dto.UserRegisterRequest;
import com.example.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    @GetMapping("/register")
    public String showForm(Model model){
        model.addAttribute("user", new UserRegisterRequest());
        return "register_form";
    }

    @PostMapping("/process-register")
    public String process(@Valid @ModelAttribute("user") UserRegisterRequest request,
                          BindingResult br,
                          RedirectAttributes ra){
        if(br.hasErrors()) return "register_form";

        if(!userService.emailUnique(null, request.getEmail())){
            br.rejectValue("email","","Email đã tồn tại!");
            return "register_form";
        }
        userService.register(request);
        ra.addFlashAttribute("message","Đăng ký thành công!");
        return "redirect:/login";
    }

    @GetMapping("/user")
    public String firstPage(Model model){ return page(1,"",model); }

    @GetMapping("/user/page/{page}")
    public String page(@PathVariable int page,
                       @RequestParam(defaultValue="") String keyword,
                       Model model){
        Page<UserDTO> pg = userService.listByPageU(page, keyword);
        model.addAttribute("currentPageU", page);
        model.addAttribute("totalPagesU", pg.getTotalPages());
        model.addAttribute("totalItemsU", pg.getTotalElements());
        model.addAttribute("listUsers", pg.getContent());
        model.addAttribute("keywordU", keyword);
        return "users";
    }
}
