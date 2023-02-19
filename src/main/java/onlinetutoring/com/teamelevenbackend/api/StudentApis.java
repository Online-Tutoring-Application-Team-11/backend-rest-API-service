package onlinetutoring.com.teamelevenbackend.api;

import onlinetutoring.com.teamelevenbackend.api.models.CreateStudentRequest;
import onlinetutoring.com.teamelevenbackend.models.StudentUser;
import onlinetutoring.com.teamelevenbackend.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RestController
@RequestMapping("/api/students")
public class StudentApis {

    @Autowired
    private StudentService studentService;

    @GetMapping(value = "/get/{email}")
    public ResponseEntity<StudentUser> getStudent(@PathVariable("email") String email) {
        try {
            return studentService.getStudentByEmail(email);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/create")
    public ResponseEntity<StudentUser> createStudent(@RequestParam CreateStudentRequest createStudentRequest) {
        try {
            return studentService.createStudent(createStudentRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
