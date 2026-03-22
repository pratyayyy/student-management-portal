package com.ija.student_management_portal.repository;

import com.ija.student_management_portal.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student,Long> {

    Optional<Student> findStudentByStudentId(String studentId);

    Optional<Student> findByStudentId(String studentId);

    Optional<Student> findByPhoneNumber(String phoneNumber);

    void deleteByStudentId(String studentId);

    // Search and pagination methods
    /**
     * Find students by name containing (case-insensitive) with pagination
     */
    Page<Student> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find students by name containing (case-insensitive) without pagination
     */
    java.util.List<Student> findByNameContainingIgnoreCase(String name);

}
