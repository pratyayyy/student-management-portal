package com.ija.student_management_portal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ija.student_management_portal.dto.PaginatedStudentResponse;
import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.entity.Student;
import com.ija.student_management_portal.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Autowired
    private ObjectMapper objectmapper;

    @Autowired
    private ProfilePictureService profilePictureService;

    /**
     * Search students by name with pagination.
     */
    @Transactional(readOnly = true)
    public PaginatedStudentResponse searchStudents(String searchTerm, int page, int pageSize) {
        log.info("Searching students with term: '{}', page: {}, pageSize: {}", searchTerm, page, pageSize);

        if (!StringUtils.hasText(searchTerm)) {
            searchTerm = "";
        }
        if (searchTerm.length() > 100) {
            searchTerm = searchTerm.substring(0, 100);
        }
        if (page < 0) page = 0;
        if (pageSize <= 0) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        try {
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<Student> studentPage = studentRepository.findByNameContainingIgnoreCase(searchTerm, pageable);

            List<StudentDTO> studentDTOs = studentPage.getContent()
                    .stream()
                    .map(entity -> objectmapper.convertValue(entity, StudentDTO.class))
                    .collect(Collectors.toList());

            log.info("Found {} students matching '{}' on page {}", studentDTOs.size(), searchTerm, page);

            return PaginatedStudentResponse.builder()
                    .content(studentDTOs)
                    .totalElements(studentPage.getTotalElements())
                    .totalPages(studentPage.getTotalPages())
                    .currentPage(page)
                    .pageSize(pageSize)
                    .hasNextPage(studentPage.hasNext())
                    .hasPreviousPage(studentPage.hasPrevious())
                    .numberOfElements(studentPage.getNumberOfElements())
                    .build();
        } catch (Exception e) {
            log.error("Error searching students with term: '{}'", searchTerm, e);
            return PaginatedStudentResponse.builder()
                    .content(Collections.emptyList())
                    .totalElements(0)
                    .totalPages(0)
                    .currentPage(page)
                    .pageSize(pageSize)
                    .hasNextPage(false)
                    .hasPreviousPage(false)
                    .numberOfElements(0)
                    .build();
        }
    }

    /**
     * Get all students with pagination.
     */
    @Transactional(readOnly = true)
    public PaginatedStudentResponse getAllStudentsWithPagination(int page, int pageSize) {
        log.info("Fetching all students with pagination - page: {}, pageSize: {}", page, pageSize);

        if (page < 0) page = 0;
        if (pageSize <= 0) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        try {
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<Student> studentPage = studentRepository.findAll(pageable);

            List<StudentDTO> studentDTOs = studentPage.getContent()
                    .stream()
                    .map(entity -> objectmapper.convertValue(entity, StudentDTO.class))
                    .collect(Collectors.toList());

            return PaginatedStudentResponse.builder()
                    .content(studentDTOs)
                    .totalElements(studentPage.getTotalElements())
                    .totalPages(studentPage.getTotalPages())
                    .currentPage(page)
                    .pageSize(pageSize)
                    .hasNextPage(studentPage.hasNext())
                    .hasPreviousPage(studentPage.hasPrevious())
                    .numberOfElements(studentPage.getNumberOfElements())
                    .build();
        } catch (Exception e) {
            log.error("Error fetching paginated students", e);
            return PaginatedStudentResponse.builder()
                    .content(Collections.emptyList())
                    .totalElements(0)
                    .totalPages(0)
                    .currentPage(page)
                    .pageSize(pageSize)
                    .hasNextPage(false)
                    .hasPreviousPage(false)
                    .numberOfElements(0)
                    .build();
        }
    }

    @Transactional
    public Optional<StudentDTO> saveStudent(StudentDTO studentDTO) {

        if (!StringUtils.hasText(studentDTO.getStudentId())) {
            throw new IllegalArgumentException("Student ID is required");
        }

        String studentId = studentDTO.getStudentId().trim();

        if (studentRepository.findStudentByStudentId(studentId).isPresent()) {
            throw new IllegalArgumentException("Student ID already exists: " + studentId);
        }

        Student student = new Student();
        student.setName(studentDTO.getName());
        student.setStandard(studentDTO.getStandard());
        student.setGuardiansName(studentDTO.getGuardiansName());
        student.setAddress(studentDTO.getAddress());
        student.setAdmissionDate(LocalDateTime.now());
        student.setPhoneNumber(studentDTO.getPhoneNumber());
        student.setAlternateNumber(studentDTO.getAlternateNumber());
        student.setStudentId(studentId);

        Student studentEntity = studentRepository.save(student);
        log.info("Saved student with student id : {}", studentEntity.getStudentId());
        StudentDTO stdDTO = objectmapper.convertValue(studentEntity, StudentDTO.class);
        return Optional.of(stdDTO);
    }

    public Optional<StudentDTO> getStudentById(String studentId) {
        log.info("Fetching student with id : {}", studentId);
        Optional<Student> studentEntity = studentRepository.findStudentByStudentId(studentId);
        StudentDTO stdDTO = objectmapper.convertValue(studentEntity, StudentDTO.class);

        // Set profilePictureUrl when a picture is stored in the database.
        if (profilePictureService.hasProfilePicture(studentId)) {
            stdDTO.setProfilePictureUrl("/students/" + studentId + "/profile-picture");
        }

        log.info("Fetched student with id {}", stdDTO.toString());
        return Optional.of(stdDTO);
    }

    @Transactional
    public Optional<StudentDTO> updateStudent(String studentId, StudentDTO studentDTO) {
        Optional<Student> existingStudentEntity = studentRepository.findStudentByStudentId(studentId);
        Student existingStudent = existingStudentEntity
                .orElseThrow(() -> new IllegalArgumentException("Student with ID " + studentId + " not found"));
        existingStudent.setName(studentDTO.getName());
        existingStudent.setStandard(studentDTO.getStandard());
        existingStudent.setGuardiansName(studentDTO.getGuardiansName());
        existingStudent.setAddress(studentDTO.getAddress());
        existingStudent.setAdmissionDate(existingStudentEntity.get().getAdmissionDate());
        existingStudent.setPhoneNumber(studentDTO.getPhoneNumber());
        existingStudent.setAlternateNumber(studentDTO.getAlternateNumber());

        Student updatedStudent = studentRepository.save(existingStudent);
        StudentDTO updatedStdDTO = objectmapper.convertValue(updatedStudent, StudentDTO.class);
        return Optional.of(updatedStdDTO);
    }

    @Transactional
    public void deleteStudentById(String studentId) {
        profilePictureService.deleteProfilePicture(studentId);
        studentRepository.deleteByStudentId(studentId);
        log.info("Student with id {} successfully deleted", studentId);
    }

    public List<StudentDTO> getAllStudents() {
        log.info("fetched all students from database.....");
        return studentRepository.findAll().stream()
                .map(entity -> objectmapper.convertValue(entity, StudentDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Upload a profile picture for a student.
     * The image is compressed and stored in the {@code student_profile_pictures} table.
     *
     * @param studentId the student ID
     * @param file      the image file to upload
     * @return updated StudentDTO with a URL to retrieve the picture
     * @throws IOException              if compression or persistence fails
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public StudentDTO uploadStudentPicture(String studentId, MultipartFile file) throws IOException {
        log.info("Uploading profile picture for student: {}", studentId);

        Optional<Student> studentOptional = studentRepository.findByStudentId(studentId);
        if (!studentOptional.isPresent()) {
            throw new IllegalArgumentException("Student with ID " + studentId + " not found");
        }

        profilePictureService.storeProfilePicture(file, studentId);

        String pictureUrl = "/students/" + studentId + "/profile-picture";
        StudentDTO studentDTO = objectmapper.convertValue(studentOptional.get(), StudentDTO.class);
        studentDTO.setProfilePictureUrl(pictureUrl);

        log.info("Successfully uploaded profile picture for student: {}", studentId);
        return studentDTO;
    }

    /**
     * Delete a student's profile picture row from the database.
     *
     * @param studentId the student ID
     * @return updated StudentDTO without a picture URL
     */
    @Transactional
    public StudentDTO deleteStudentPicture(String studentId) {
        log.info("Deleting profile picture for student: {}", studentId);

        Optional<Student> studentOptional = studentRepository.findByStudentId(studentId);
        if (!studentOptional.isPresent()) {
            throw new IllegalArgumentException("Student with ID " + studentId + " not found");
        }

        profilePictureService.deleteProfilePicture(studentId);
        log.info("Successfully deleted profile picture for student: {}", studentId);
        return objectmapper.convertValue(studentOptional.get(), StudentDTO.class);
    }
}
