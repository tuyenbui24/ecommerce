package com.example.ecommerce.staff.repository;

import com.example.ecommerce.staff.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {

    @Query("update Staff s set s.enabled = ?2 where s.id = ?1")
    @Modifying
    @Transactional
    public void updateEnabled(int id, boolean enabled);

    @Query("select s from Staff s where s.email = :email")
    public Staff getStaffByEmail(@Param("email") String email);

    @Query("select s from Staff s where concat(s.id, ' ',s.email, ' ',s.firstName, ' ',s.lastName) like %?1%")
    public Page<Staff> searchS(String keyword, Pageable pageable);

}
