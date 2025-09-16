package com.example.ecommerce.user.repo;

import com.example.ecommerce.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer>{

    @Query("SELECT u FROM User u WHERE u.email = :email")
    User getUserByEmail(@Param("email") String email);

    @Query("SELECT u.id FROM User u WHERE lower(u.email) = lower(:email)")
    Optional<Integer> findIdByEmail(@Param("email") String email);

    @Query("""
           SELECT u FROM User u
           WHERE CONCAT(u.id,' ',u.email,' ',u.firstName,' ',u.lastName)
                 LIKE %?1%
           """)
    Page<User> search(String keyword, Pageable pageable);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
