package com.example.ecommerce.user.service;

import com.example.ecommerce.cart.repo.CartRepository;
import com.example.ecommerce.config.exception.UserNotFoundExp;
import com.example.ecommerce.order.repo.OrderRepository;
import com.example.ecommerce.role.entity.Role;
import com.example.ecommerce.role.repository.RoleRepository;
import com.example.ecommerce.user.dto.UserDTO;
import com.example.ecommerce.user.dto.UserProfileUpdateRequest;
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
    private final OrderRepository orderRepo;
    private final CartRepository cartRepo;

    public UserService(UserRepository userRepo, RoleRepository roleRepo,
                       PasswordEncoder encoder, OrderRepository orderRepo, CartRepository cartRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
        this.orderRepo = orderRepo;
        this.cartRepo = cartRepo;
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
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + id));
    }

    public User getById(Integer id) {
        return userRepo.findById(id).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
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

    public void deleteById(Integer id) throws UsernameNotFoundException {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundExp("Khng tìm thấy người dùng vs ID: " + id));
        if (orderRepo.existsByUser_Id(id)) {
            throw new IllegalStateException("Không thể xoá: người dùng đã có đơn hàng.");
        }
        cartRepo.findByUserId(id).ifPresent(cartRepo::delete);

        user.getRoles().clear();
        userRepo.save(user);

        userRepo.delete(user);
    }

    public void updateUserProfile(Integer userId, UserProfileUpdateRequest req) {
        User user = getById(userId);

        if (req.getEmail() != null && !req.getEmail().isBlank() && !user.getEmail().equals(req.getEmail())) {
            if (userRepo.existsByEmail(req.getEmail())) {
                throw new IllegalArgumentException("Email đã tồn tại!");
            }
            user.setEmail(req.getEmail());
        }

        if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if (req.getLastName()  != null) user.setLastName(req.getLastName());
        userRepo.save(user);
    }

    public void updatePassword(Integer userId, String newPassword) {
        User user = getById(userId);
        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);
    }

    public Integer findIdByUsername(String username) {
        return userRepo.findIdByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));
    }

    @Transactional(readOnly = true)
    public List<User> findForExport(String keyword) {
        Pageable pageable = Pageable.unpaged();

        Page<User> page = (keyword == null || keyword.isBlank())
                ? userRepo.findAll(pageable)
                : userRepo.search(keyword, pageable);

        return page.getContent();
    }
}
