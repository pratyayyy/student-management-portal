package com.ija.student_management_portal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.entity.Student;
import com.ija.student_management_portal.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Optional<StudentDTO> saveStudent(StudentDTO studentDTO){
        Student student = new Student();
        student.setName(studentDTO.getName());
        student.setStandard(studentDTO.getStandard());
        student.setGuardiansName(studentDTO.getGuardiansName());
        student.setAddress(studentDTO.getAddress());
        student.setAdmissionDate(studentDTO.getAdmissionDate());
        student.setPhoneNumber(studentDTO.getPhoneNumber());
        student.setAlternateNumber(studentDTO.getAlternateNumber());

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
