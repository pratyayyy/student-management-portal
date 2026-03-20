package com.ija.student_management_portal.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_credentials")
@Getter
@Setter
public class UserCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String studentId;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean enabled;

    public enum UserRole {
        STUDENT, ADMIN
    }
}
