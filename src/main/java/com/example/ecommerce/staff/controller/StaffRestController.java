package com.example.ecommerce.staff.controller;

import com.example.ecommerce.config.FileUpload;
import com.example.ecommerce.config.exception.UserNotFoundExp;
import com.example.ecommerce.role.dto.RoleDTO;
import com.example.ecommerce.staff.dto.StaffCreateRequest;
import com.example.ecommerce.staff.dto.StaffDTO;
import com.example.ecommerce.staff.entity.Staff;
import com.example.ecommerce.staff.mapper.StaffMapper;
import com.example.ecommerce.staff.service.StaffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/staffs")
public class StaffRestController {

    private final StaffService staffService;

    public StaffRestController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStaffList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword) {
        try {
            Page<StaffDTO> staffPage = staffService.listStaffDTOByPage(page, keyword);

            Map<String, Object> res = new HashMap<>();
            res.put("data", staffPage.getContent());
            res.put("currentPage", page);
            res.put("totalItems", staffPage.getTotalElements());
            res.put("totalPages", staffPage.getTotalPages());

            return ResponseEntity.ok(res);
        } catch (Exception ex) {
            log.error("GET /api/staffs failed: page={}, keyword='{}'", page, keyword, ex);
            throw ex;
        }
    }


//    @GetMapping
//    public ResponseEntity<Map<String, Object>> getStaffList(
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(required = false) String keyword) {
//
//        List<StaffDTO> list = staffService.listStaffDTOByPage(page, keyword);
//        int totalItems = staffService.countTotalStaff(keyword); // hoặc lấy từ Page
//        int totalPages = (int) Math.ceil((double) totalItems / StaffService.STAFFS_IN_PAGE);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("data", list);
//        response.put("currentPage", page);
//        response.put("totalItems", totalItems);
//        response.put("totalPages", totalPages);
//
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/{id}/upload-photo")
    public ResponseEntity<?> uploadPhoto(
            @PathVariable Integer id,
            @RequestParam("imageFile") MultipartFile multipartFile) throws IOException {

        Staff staff = staffService.getId(id);
        if (staff == null) return ResponseEntity.notFound().build();

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        staff.setPhotos(fileName);
        StaffCreateRequest request = StaffMapper.toRequest(staff);

        staffService.save(request);

        String uploadDir = "staff-photos/" + id;
        FileUpload.cleanDir(uploadDir);
        FileUpload.saveFile(uploadDir, fileName, multipartFile);

        return ResponseEntity.ok("Upload thành công");
    }

    //Lấy ảnh (nếu không public)
//    @GetMapping("/{id}/photo")
//    public ResponseEntity<Resource> getPhoto(@PathVariable Integer id) throws IOException {
//        Staff staff = staffService.getId(id);
//        if (staff == null || staff.getPhotos() == null) {
//            return ResponseEntity.notFound().build();
//        }
//
//        String uploadDir = "staff-photos/" + id;
//        Path filePath = Paths.get(uploadDir).resolve(staff.getPhotos());
//
//        if (!Files.exists(filePath)) {
//            return ResponseEntity.notFound().build();
//        }
//
//        Resource resource = new UrlResource(filePath.toUri());
//        String contentType = Files.probeContentType(filePath);
//        if (contentType == null) contentType = "application/octet-stream";
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(contentType))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + staff.getPhotos() + "\"")
//                .body(resource);
//    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(staffService.findAllRolesDTO());
    }

    @PostMapping
    public ResponseEntity<StaffDTO> createStaff(@RequestBody StaffCreateRequest request) {
        StaffDTO saved = staffService.save(request);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StaffDTO> updateStaff(@PathVariable Integer id, @RequestBody StaffCreateRequest request) {
        request.setId(id);
        StaffDTO saved = staffService.save(request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Staff> getStaff(@PathVariable Integer id) throws UserNotFoundExp {
        return ResponseEntity.ok(staffService.getId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Integer id) throws UserNotFoundExp {
        staffService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Integer id, @RequestParam boolean enabled) {
        staffService.updateStatus(id, enabled);
        return ResponseEntity.noContent().build();
    }

    // Kiểm tra email có duy nhất không (dùng khi thêm/sửa nhân viên)
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailUnique(
            @RequestParam Integer id,
            @RequestParam String email) {
        boolean isUnique = staffService.isEmailUnique(id, email);
        return ResponseEntity.ok(isUnique);
    }
}
