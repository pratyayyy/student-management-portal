package com.ija.student_management_portal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ija.student_management_portal.dto.PaginatedStudentResponse;
import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.entity.Student;
import com.ija.student_management_portal.entity.StudentRollCounter;
import com.ija.student_management_portal.repository.StudentRepository;
import com.ija.student_management_portal.repository.StudentRollCounterRepository;
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
    public StudentService(StudentRepository studentRepository){
        this.studentRepository = studentRepository;
    }

    @Autowired
    private ObjectMapper objectmapper;

    @Autowired
    private StudentRollCounterRepository studentRollCounterRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // ... existing methods ...

    /**
     * Search students by name with pagination
     * @param searchTerm the search term (name)
     * @param page the page number (0-indexed)
     * @param pageSize the number of records per page
     * @return paginated response with student data
     */
    @Transactional(readOnly = true)
    public PaginatedStudentResponse searchStudents(String searchTerm, int page, int pageSize) {
        log.info("Searching students with term: '{}', page: {}, pageSize: {}", searchTerm, page, pageSize);

        // Validate input
        if (!StringUtils.hasText(searchTerm)) {
            searchTerm = "";
        }

        // Limit search term length for security
        if (searchTerm.length() > 100) {
            searchTerm = searchTerm.substring(0, 100);
        }

        // Ensure valid page and pageSize values
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
     * Get all students with pagination
     * @param page the page number (0-indexed)
     * @param pageSize the number of records per page
     * @return paginated response with all students
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
    public Optional<StudentDTO> saveStudent(StudentDTO studentDTO){

        int admissionYear = LocalDateTime.now().getYear();
        StudentRollCounter counter = studentRollCounterRepository.findForUpdate(admissionYear)
                .orElseGet(() -> {
                    StudentRollCounter c = new StudentRollCounter();
                    c.setAdmissionYear(admissionYear);
                    c.setLastNumber(0);
                    return studentRollCounterRepository.save(c);
                });

        counter.setLastNumber(counter.getLastNumber() + 1);
        studentRollCounterRepository.saveAndFlush(counter);

        String studentId = admissionYear + "-" + String.format("%04d", counter.getLastNumber());


        Student student = new Student();
        student.setName(studentDTO.getName());
        student.setStandard(studentDTO.getStandard());
        student.setGuardiansName(studentDTO.getGuardiansName());
        student.setAddress(studentDTO.getAddress());
        student.setAdmissionDate(LocalDateTime.now());
        student.setPhoneNumber(studentDTO.getPhoneNumber());
        student.setAlternateNumber(studentDTO.getAlternateNumber());
        student.setStudentId(studentId);

        Student studentEntity =  studentRepository.save(student);
        log.info("Saved student with student id : {}", studentEntity.getStudentId());
        StudentDTO stdDTO =  objectmapper.convertValue(studentEntity,StudentDTO.class);
        return Optional.of(stdDTO);
    }

    public Optional<StudentDTO> getStudentById(String studentId){
        log.info("Fetching student with id : {}", studentId);
        Optional<Student> studentEntity = studentRepository.findStudentByStudentId(studentId);
        StudentDTO stdDTO = objectmapper.convertValue(studentEntity,StudentDTO.class);
        log.info("Fetched student with id {}", stdDTO.toString());
        return Optional.of(stdDTO);
    }

    @Transactional
    public Optional<StudentDTO> updateStudent(String studentId,StudentDTO studentDTO){
        Optional<Student> existingStudentEntity = studentRepository.findStudentByStudentId(studentId);
        Student existingStudent = existingStudentEntity.orElseGet(null);
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
    public void deleteStudentById(String studentId){
         studentRepository.deleteByStudentId(studentId);
         log.info("Student with id {} successfully deleted", studentId);
    }

    public List<StudentDTO> getAllStudents(){
        log.info("fetched all students from database.....");
        return studentRepository.findAll().stream()
                .map(entity -> objectmapper.convertValue(entity, StudentDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Upload a profile picture for a student
     * @param studentId the student ID
     * @param file the image file to upload
     * @return updated StudentDTO with picture URL
     * @throws IOException if file upload fails
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public StudentDTO uploadStudentPicture(String studentId, MultipartFile file) throws IOException {
        log.info("Uploading profile picture for student: {}", studentId);

        Optional<Student> studentOptional = studentRepository.findByStudentId(studentId);
        if (!studentOptional.isPresent()) {
            throw new IllegalArgumentException("Student with ID " + studentId + " not found");
        }

        Student student = studentOptional.get();

        try {
            // Delete old picture if exists
            if (student.getProfilePictureStoragePath() != null && !student.getProfilePictureStoragePath().isEmpty()) {
                try {
                    fileStorageService.deleteProfilePicture(student.getProfilePictureStoragePath());
                } catch (IOException e) {
                    log.warn("Failed to delete old picture for student: {}", studentId, e);
                }
            }

            // Upload new picture
            String storagePath = fileStorageService.uploadProfilePicture(file, studentId);
            student.setProfilePictureStoragePath(storagePath);

            Student updatedStudent = studentRepository.save(student);
            StudentDTO studentDTO = objectmapper.convertValue(updatedStudent, StudentDTO.class);
            studentDTO.setProfilePictureUrl(fileStorageService.getProfilePictureUrl(storagePath));

            log.info("Successfully uploaded profile picture for student: {}", studentId);
            return studentDTO;

        } catch (IOException e) {
            log.error("Failed to upload profile picture for student: {}", studentId, e);
            throw e;
        }
    }

    /**
     * Delete a student's profile picture
     * @param studentId the student ID
     * @return updated StudentDTO without picture
     * @throws IOException if file deletion fails
     */
    @Transactional
    public StudentDTO deleteStudentPicture(String studentId) throws IOException {
        log.info("Deleting profile picture for student: {}", studentId);

        Optional<Student> studentOptional = studentRepository.findByStudentId(studentId);
        if (!studentOptional.isPresent()) {
            throw new IllegalArgumentException("Student with ID " + studentId + " not found");
        }

        Student student = studentOptional.get();

        try {
            if (student.getProfilePictureStoragePath() != null && !student.getProfilePictureStoragePath().isEmpty()) {
                fileStorageService.deleteProfilePicture(student.getProfilePictureStoragePath());
                student.setProfilePictureStoragePath(null);

                Student updatedStudent = studentRepository.save(student);
                log.info("Successfully deleted profile picture for student: {}", studentId);
                return objectmapper.convertValue(updatedStudent, StudentDTO.class);
            }

            return objectmapper.convertValue(student, StudentDTO.class);

        } catch (IOException e) {
            log.error("Failed to delete profile picture for student: {}", studentId, e);
            throw e;
        }
    }

}
