
package com.example.ecommerce.user.service;

import com.example.ecommerce.role.entity.Role;
import com.example.ecommerce.role.repository.RoleRepository;
import com.example.ecommerce.user.dto.UserDTO;
import com.example.ecommerce.user.dto.UserRegisterRequest;
import com.example.ecommerce.user.entity.User;
import com.example.ecommerce.user.mapper.UserMapper;
import com.example.ecommerce.user.repo.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
    }

    @Transactional
    public void register(UserRegisterRequest request){
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(encoder.encode(request.getPassword()));

        Role roleUser = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER thiếu"));
        user.addRole(roleUser);

        userRepo.save(user);
    }

    public static final int USERS_IN_PAGE = 5;

    public Page<UserDTO> listByPageU(int pageNum, String keyword){
        Pageable pageable = PageRequest.of(pageNum-1, USERS_IN_PAGE, Sort.by("lastName").ascending());
        Page<User> page = (keyword == null || keyword.isBlank())
                ? userRepo.findAll(pageable)
                : userRepo.search(keyword, pageable);
        List<UserDTO> dtos = page.map(UserMapper::toDTO).toList();
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    public boolean emailUnique(Integer id, String email){
        User user = userRepo.getUserByEmail(email);
        return user == null || user.getId().equals(id);
    }

    public UserDTO findDtoById(Integer id) {
        return userRepo.findById(id)
                .map(UserMapper::toDTO)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));
    }

    public User getById(Integer id) {
        return userRepo.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public boolean checkPassword(User user, String rawPassword) {
        return encoder.matches(rawPassword, user.getPassword());
    }

    public void updateUserInfo(UserDTO dto, String newPassword) {
        User user = userRepo.findById(dto.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));

        if (!user.getEmail().equals(dto.getEmail())) {
            if (userRepo.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email đã được sử dụng");
            }
            user.setEmail(dto.getEmail());
        }

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());

        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(encoder.encode(newPassword));
        }

        userRepo.save(user);
    }

}
