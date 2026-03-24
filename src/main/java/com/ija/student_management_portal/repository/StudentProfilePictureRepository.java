package com.ija.student_management_portal.repository;

import com.ija.student_management_portal.entity.StudentProfilePicture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for student profile picture data stored in the database.
 */
public interface StudentProfilePictureRepository extends JpaRepository<StudentProfilePicture, Long> {

    Optional<StudentProfilePicture> findByStudentId(String studentId);

    void deleteByStudentId(String studentId);

    boolean existsByStudentId(String studentId);
}
