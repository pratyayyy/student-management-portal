package com.ija.student_management_portal.controller;

import com.ija.student_management_portal.dto.StudentDTO;
import com.ija.student_management_portal.entity.Student;
import com.ija.student_management_portal.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(StudentController.STUDENT_API_ENDPOINT)
public class StudentController {
    public static final String STUDENT_API_ENDPOINT="/studentmanagement";
    public static final String CREATE_STUDENT_API = "/createstudent";
    public static final String DISPLAY_STUDENT_API = "/displaystudent/{id}";
    public static final String UPDATE_STUDENT_API = "/updatestudent/{id}";
    public static final String DELETE_STUDENT_API = "/deletestudent/{id}";

    @Autowired
    private StudentService studentService;

    @PostMapping(CREATE_STUDENT_API)
    public ResponseEntity<?> createStudent(@RequestBody StudentDTO studentDTO){
        Optional<StudentDTO> saveStudent = studentService.saveStudent(studentDTO);
            if(saveStudent.isPresent()){
                return ResponseEntity.status(HttpStatus.CREATED).body(saveStudent.get());
            }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Student could not be created");
    }

    @GetMapping(DISPLAY_STUDENT_API)
    public ResponseEntity<?> getStudent(@PathVariable Long id){
        Optional<StudentDTO> showStudentDTO = studentService.getStudentById(id);
        if(showStudentDTO.isPresent()){
            return ResponseEntity.status(HttpStatus.OK).body(showStudentDTO.get());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Student not found");
    }


    @PatchMapping(UPDATE_STUDENT_API)
    public ResponseEntity<?> updateStudent(@RequestBody StudentDTO studentDTO, @PathVariable Long id){

        Optional<StudentDTO> updateStudentDTO  = studentService.updateStudent(id, studentDTO);
        if (updateStudentDTO.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(updateStudentDTO.get());
        }
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Student not found.");

    }


    @DeleteMapping(DELETE_STUDENT_API)
    public ResponseEntity<String> deleteStudent(@PathVariable Long id){

        try
        {
            studentService.deleteStudentById(id);
            return ResponseEntity.ok("Student deleted successfully");
        }
        catch(Exception e)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }

}
