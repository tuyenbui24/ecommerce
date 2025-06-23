package com.example.ecommerce.staff.controller;

import com.example.ecommerce.config.FileUpload;
import com.example.ecommerce.staff.dto.StaffCreateRequest;
import com.example.ecommerce.staff.dto.StaffDTO;
import com.example.ecommerce.staff.entity.Staff;
import com.example.ecommerce.staff.mapper.StaffMapper;
import com.example.ecommerce.staff.service.StaffService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
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
        StaffCreateRequest staff = new StaffCreateRequest();
        staff.setEnabled(true);
        model.addAttribute("staff", staff);
        model.addAttribute("listRoles", staffService.findAllRoles());
        model.addAttribute("pageTitle", "Create New Staff");
        return "staff_form";
    }

    @PostMapping("/save")
    public String saveStaff(@ModelAttribute("staff") StaffCreateRequest request,
                            @RequestParam("imageFile") MultipartFile multipartFile,
                            RedirectAttributes redirectAttributes) throws IOException {

        if (!staffService.isEmailUnique(request.getId(), request.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email đã tồn tại! Vui lòng sử dụng email khác.");
            return request.getId() == null ? "redirect:/staff/new" : "redirect:/staff/edit/" + request.getId();
        }

        if (!multipartFile.isEmpty()) {
            String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
            request.setPhotos(fileName);
        }

        Staff saved = staffService.save(request);

        if (!multipartFile.isEmpty()) {
            String uploadDir = "staff-photos/" + saved.getId();
            FileUpload.cleanDir(uploadDir);
            FileUpload.saveFile(uploadDir, saved.getPhotos(), multipartFile);
        }

        redirectAttributes.addFlashAttribute("message", "Lưu nhân viên thành công.");
        return "redirect:/staff";
    }

    @GetMapping("/edit/{id}")
    public String editStaff(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Staff staff = staffService.getId(id);
        StaffCreateRequest request = StaffMapper.toRequest(staff);

        model.addAttribute("staff", request);
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
