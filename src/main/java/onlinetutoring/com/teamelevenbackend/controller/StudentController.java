package onlinetutoring.com.teamelevenbackend.controller;

import onlinetutoring.com.teamelevenbackend.controller.models.UpdateStudentRequest;
import onlinetutoring.com.teamelevenbackend.models.StudentUser;
import onlinetutoring.com.teamelevenbackend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static onlinetutoring.com.teamelevenbackend.utils.ControllerUtils.BASE_LOCAL;
import static onlinetutoring.com.teamelevenbackend.utils.ControllerUtils.BASE_PRODUCTION;

/**
 * Controller class for handling student related API endpoints.
 */
@Controller
@RestController
@RequestMapping("/students")
@CrossOrigin(origins = {BASE_PRODUCTION, BASE_LOCAL}, methods = {RequestMethod.GET, RequestMethod.PUT})
public class StudentController {

    private StudentService studentService;

    @Autowired
    public void setStudentService(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * Get a student by email.
     *
     * @param email   The email of the student to retrieve.
     * @return ResponseEntity   Contains StudentUser object.
     * @throws Exception   The student is not found.
     */
    @GetMapping(value = "/get/{email}")
    public ResponseEntity<StudentUser> getStudent(@PathVariable(value = "email", required = false) String email) {
        try {
            return studentService.getStudentByEmail(email);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Update an existing student.
     *
     * @param updateStudentRequest  Object containing the updated student information.
     * @return ResponseEntity   Contains updated StudentUser object.
     * @throws Exception   Couldn't update the student.
     */
    @PutMapping(value = "/update")
    public ResponseEntity<StudentUser> updateStudent(@RequestBody UpdateStudentRequest updateStudentRequest) {
        try {
            return studentService.updateStudent(updateStudentRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
