package com.example.ecommerce.staff.entity;

import com.example.ecommerce.role.entity.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"photos", "enabled"})
@EqualsAndHashCode(of = "id")
@Builder
@Entity
@Table(name = "staffs")
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 130, unique = true, nullable = false)
    private String email;

    @Column(length = 250, nullable = false)
    private String password;

    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @Column(length = 50)
    private String photos;

    @Column(nullable = false)
    private boolean enabled;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "staff_roles",
            joinColumns = @JoinColumn(name = "staff_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public Staff(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = true;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    @Transient
    public String getPhotosImagePath() {
        if (id == null || photos == null) return "/images/default-staff.png";
        return "/staff-photos/" + this.id + "/" + this.photos;
    }

    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
}