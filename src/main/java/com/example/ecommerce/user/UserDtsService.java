package com.example.ecommerce.user;

import com.example.ecommerce.user.entity.User;
import com.example.ecommerce.user.repo.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDtsService implements UserDetailsService {

    private final UserRepository userRepo;

    public UserDtsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.getUserByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng: " + email);
        }
        return new UserDts(user);
    }
}