package com.ija.student_management_portal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.entity.Student;
import com.ija.student_management_portal.entity.StudentRollCounter;
import com.ija.student_management_portal.repository.StudentRepository;
import com.ija.student_management_portal.repository.StudentRollCounterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

}
