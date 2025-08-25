//package com.ija.student_management_portal.controller;
//
//import com.ija.student_management_portal.dto.StudentDTO;
//import com.ija.student_management_portal.entity.Student;
//import com.ija.student_management_portal.service.StudentService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//
//@RestController
//@RequestMapping(StudentController.STUDENT_API_ENDPOINT)
//@Slf4j
//public class StudentController {
//    public static final String STUDENT_API_ENDPOINT="/studentmanagement";
//    public static final String CREATE_STUDENT_API = "/createstudent";
//    public static final String DISPLAY_STUDENT_API = "/displaystudent/{studentId}";
//    public static final String UPDATE_STUDENT_API = "/updatestudent/{studentId}";
//    public static final String DELETE_STUDENT_API = "/deletestudent/{studentId}";
//
//    @Autowired
//    private StudentService studentService;
//
//    @PostMapping(CREATE_STUDENT_API)
//    public ResponseEntity<?> createStudent(@RequestBody StudentDTO studentDTO){
//        log.info("Saving Student Details for {}", studentDTO.toString());
//        Optional<StudentDTO> saveStudent = studentService.saveStudent(studentDTO);
//            if(saveStudent.isPresent()){
//                return ResponseEntity.status(HttpStatus.CREATED).body(saveStudent.get());
//            }
//        return ResponseEntity.status(HttpStatus.CONFLICT).body("Student could not be created");
//    }
//
//    @GetMapping(DISPLAY_STUDENT_API)
//    public ResponseEntity<?> getStudent(@PathVariable String studentId){
//        Optional<StudentDTO> showStudentDTO = studentService.getStudentById(studentId);
//        if(showStudentDTO.isPresent()){
//            return ResponseEntity.status(HttpStatus.OK).body(showStudentDTO.get());
//        }
//        return ResponseEntity.status(HttpStatus.CONFLICT).body("Student not found");
//    }
//
//
//    @PatchMapping(UPDATE_STUDENT_API)
//    public ResponseEntity<?> updateStudent(@RequestBody StudentDTO studentDTO, @PathVariable String studentId){
//
//        Optional<StudentDTO> updateStudentDTO  = studentService.updateStudent(studentId, studentDTO);
//        if (updateStudentDTO.isPresent()) {
//            return ResponseEntity.status(HttpStatus.OK).body(updateStudentDTO.get());
//        }
//            return ResponseEntity.status(HttpStatus.CONFLICT).body("Student not found.");
//
//    }
//
//
//    @DeleteMapping(DELETE_STUDENT_API)
//    public ResponseEntity<String> deleteStudent(@PathVariable String studentId){
//
//        try
//        {
//            studentService.deleteStudentById(studentId);
//            return ResponseEntity.ok("Student deleted successfully");
//        }
//        catch(Exception e)
//        {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        }
//
//    }
//
//}
