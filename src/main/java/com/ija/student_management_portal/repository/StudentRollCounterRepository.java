package com.ija.student_management_portal.repository;

import com.ija.student_management_portal.entity.StudentRollCounter;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface StudentRollCounterRepository extends JpaRepository<StudentRollCounter, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM StudentRollCounter c WHERE c.admissionYear = :year")
    Optional<StudentRollCounter> findForUpdate(@Param("year") int year);
}
