package org.acme.auth.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.acme.common.Role;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank
    @Column(nullable = false)
    public String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    public String email;

    @NotBlank
    @Column(nullable = false)
    public String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Role role;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "active", nullable = false)
    public boolean active = true;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}