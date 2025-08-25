package com.ija.student_management_portal.repository;

import com.ija.student_management_portal.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student,Long> {

    Optional<Student> findStudentByStudentId(String studentId);

    void deleteByStudentId(String studentId);

}
