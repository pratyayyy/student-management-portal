package com.ija.student_management_portal.repository;

import com.ija.student_management_portal.entity.SiteContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SiteContentRepository extends JpaRepository<SiteContent, Long> {

    Optional<SiteContent> findByContentKey(String contentKey);

    boolean existsByContentKey(String contentKey);
}
