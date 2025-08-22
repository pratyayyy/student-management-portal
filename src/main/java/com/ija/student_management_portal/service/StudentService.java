package com.ija.student_management_portal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.entity.Student;
import com.ija.student_management_portal.entity.StudentRollCounter;
import com.ija.student_management_portal.repository.StudentRepository;
import com.ija.student_management_portal.repository.StudentRollCounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
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

        int admissionYear = studentDTO.getAdmissionDate().getYear();
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
        student.setAdmissionDate(studentDTO.getAdmissionDate());
        student.setPhoneNumber(studentDTO.getPhoneNumber());
        student.setAlternateNumber(studentDTO.getAlternateNumber());
        student.setStudentId(studentId);

        Student studentEntity =  studentRepository.save(student);
        StudentDTO stdDTO =  objectmapper.convertValue(studentEntity,StudentDTO.class);
        return Optional.of(stdDTO);
    }

    public Optional<StudentDTO> getStudentById(Long id){
        Optional<Student> studentEntity = studentRepository.findById(id);
        StudentDTO stdDTO = objectmapper.convertValue(studentEntity,StudentDTO.class);
        return Optional.of(stdDTO);
    }

    public Optional<StudentDTO> updateStudent(Long id,StudentDTO studentDTO){
        Optional<Student> existingStudentEntity = studentRepository.findById(id);
        Student existingStudent = existingStudentEntity.orElseGet(null);
        existingStudent.setName(studentDTO.getName());
        existingStudent.setStandard(studentDTO.getStandard());
        existingStudent.setGuardiansName(studentDTO.getGuardiansName());
        existingStudent.setAddress(studentDTO.getAddress());
        existingStudent.setAdmissionDate(studentDTO.getAdmissionDate());
        existingStudent.setPhoneNumber(studentDTO.getPhoneNumber());
        existingStudent.setAlternateNumber(studentDTO.getAlternateNumber());

        Student updatedStudent = studentRepository.save(existingStudent);
        StudentDTO updatedStdDTO = objectmapper.convertValue(updatedStudent, StudentDTO.class);
        return Optional.of(updatedStdDTO);
    }

    public void deleteStudentById(Long id){
         studentRepository.deleteById(id);
    }

}
