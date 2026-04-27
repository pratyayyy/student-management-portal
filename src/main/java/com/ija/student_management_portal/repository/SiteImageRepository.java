package com.ija.student_management_portal.repository;

import com.ija.student_management_portal.entity.SiteImage;
import com.ija.student_management_portal.entity.SiteImage.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteImageRepository extends JpaRepository<SiteImage, Long> {

    List<SiteImage> findByImageTypeOrderBySortOrderAscUploadedAtAsc(ImageType imageType);

    List<SiteImage> findAllByOrderByImageTypeAscSortOrderAscUploadedAtAsc();
}
