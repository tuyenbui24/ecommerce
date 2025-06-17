package com.example.ecommerce.user.repo;

import com.example.ecommerce.user.entity.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Integer>{

    @Query("SELECT u FROM User u WHERE u.email = :email")
    User getUserByEmail(@Param("email") String email);

    @Query("""
           SELECT u FROM User u
           WHERE CONCAT(u.id,' ',u.email,' ',u.firstName,' ',u.lastName)
                 LIKE %?1%
           """)
    Page<User> search(String keyword, Pageable pageable);

    boolean existsByEmail(String email);

}
