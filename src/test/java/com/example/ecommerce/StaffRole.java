package com.example.ecommerce;

import com.example.ecommerce.role.entity.Role;
import com.example.ecommerce.role.repository.RoleRepository;
import com.example.ecommerce.staff.entity.Staff;
import com.example.ecommerce.staff.repository.StaffRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
public class StaffRole {
    private StaffRepository staffRepository;
    private RoleRepository roleRepository;
    private TestEntityManager entityManager;

    public StaffRole(StaffRepository staffRepository, RoleRepository roleRepository, TestEntityManager entityManager) {
        this.staffRepository = staffRepository;
        this.roleRepository = roleRepository;
        this.entityManager = entityManager;
    }

    @Test
    public void testCreateRole() {
        Role roleAdmin = new Role("Admin", "quản lý mọi thứ");
        Role roleUser = new Role("User", "khách hàng");

        Role roleSalesPerson = new Role("Nhân viên bán hàng",
                "quản lý giá sản phẩm, khách hàng, vận chuyển, đơn hàng và báo cáo bán hàng");

        Role roleEditor = new Role("Biên tập viên",
                "quản lý danh mục, thương hiệu, sản phẩm, bài viết và menu");

        Role roleShipper = new Role("Nhân viên giao hàng",
                "xem sản phẩm, xem đơn hàng và cập nhật trạng thái đơn hàng");

        Role roleAssistant = new Role("Trợ lý",
                "quản lý câu hỏi và đánh giá");

        roleRepository.save(roleAdmin);
        roleRepository.save(roleUser);
        roleRepository.save(roleSalesPerson);
        roleRepository.save(roleEditor);
        roleRepository.save(roleShipper);
        roleRepository.save(roleAssistant);
    }

    @Test
    public void testCreateUserWithRole() {
        Role roleAdmin = entityManager.find(Role.class, 1);
        Staff staff = new Staff("quangtuyen10x@gmail.com", "buituyen10x", "Bùi", "Tuyến");
        staff.addRole(roleAdmin);

        Staff savedUser = staffRepository.save(staff);
        assertThat(savedUser.getId()).isGreaterThan(0);
    }
}
