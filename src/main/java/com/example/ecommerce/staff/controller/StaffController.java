package com.example.ecommerce.staff.controller;

import com.example.ecommerce.staff.dto.StaffDTO;
import com.example.ecommerce.staff.entity.Staff;
import com.example.ecommerce.staff.service.StaffService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public String listFirstPage(Model model) {
        return page(1, model, "");
    }

    @GetMapping("/page/{pageNum}")
    public String page(@PathVariable int pageNum, Model model,
                       @RequestParam(defaultValue = "") String keyword) {

        List<StaffDTO> listStaffs = staffService.listStaffDTOByPage(pageNum, keyword);

        int start = (pageNum - 1) * StaffService.STAFFS_IN_PAGE + 1;
        int end = start + listStaffs.size() - 1;

        model.addAttribute("currentPages", pageNum);
        model.addAttribute("startStaff", start);
        model.addAttribute("endStaff", end);
        model.addAttribute("totalStaff", listStaffs.size());
        model.addAttribute("listStaffs", listStaffs);
        model.addAttribute("totalPages", (int) Math.ceil((double) listStaffs.size() / StaffService.STAFFS_IN_PAGE));
        model.addAttribute("keyword", keyword);

        return "staffs";
    }

    @GetMapping("/new")
    public String newStaff(Model model) {
        Staff staff = new Staff();
        staff.setEnabled(true);
        model.addAttribute("staff", staff);
        model.addAttribute("listRoles", staffService.findAllRoles());
        model.addAttribute("pageTitle", "Create New Staff");
        return "staff_form";
    }

    @PostMapping("/save")
    public String saveStaff(Staff staff, RedirectAttributes redirectAttributes) {
        if (!staffService.isEmailUnique(staff.getId(), staff.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email đã tồn tại! Vui lòng sử dụng email khác.");
            return staff.getId() == null ? "redirect:/new" : "redirect:/edit/" + staff.getId();
        }
        staffService.save(staff);
        redirectAttributes.addFlashAttribute("message", "Staff saved successfully.");
        return "redirect:/staff";
    }

    @GetMapping("/edit/{id}")
    public String editStaff(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Staff staff = staffService.getId(id);
        model.addAttribute("staff", staff);
        model.addAttribute("listRoles", staffService.findAllRoles());
        model.addAttribute("pageTitle", "Edit Staff (ID: " + id + ")");
        return "staff_form";
    }

    @GetMapping("/delete/{id}")
    public String deleteStaff(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        staffService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Staff ID " + id + " deleted successfully.");
        return "redirect:/staff";
    }

    @GetMapping("/{id}/enabled/{status}")
    public String toggleStatus(@PathVariable Integer id, @PathVariable boolean status,
                               RedirectAttributes redirectAttributes) {
        staffService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("message", "Staff ID " + id + " has been " + (status ? "enabled" : "disabled"));
        return "redirect:/staff";
    }
}
