package com.ija.student_management_portal.repository;

import com.ija.student_management_portal.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    Optional<UserCredential> findByUsername(String username);
    Optional<UserCredential> findByStudentId(String studentId);
    long countByRole(UserCredential.UserRole role);
}
