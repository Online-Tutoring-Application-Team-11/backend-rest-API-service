package onlinetutoring.com.teamelevenbackend.controller;

import onlinetutoring.com.teamelevenbackend.models.TutorUser;
import onlinetutoring.com.teamelevenbackend.controller.models.UpdateTutorRequest;
import onlinetutoring.com.teamelevenbackend.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RestController
@RequestMapping("/tutors")
public class TutorController {

    private TutorService tutorService;
    @Autowired
    public void setTutorService(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    @GetMapping(value = "/get/all")
    public ResponseEntity<List<TutorUser>> getTutors() {
        try {
            return tutorService.getAllTutors();
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/{email}")
    public ResponseEntity<TutorUser> getTutor(@PathVariable("email") String email) {
        try{
            return tutorService.getTutorByEmail(email);
        }catch (Exception ex){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/delete/{email}")
    public ResponseEntity<HttpStatus> deleteStudent(@PathVariable("email") String email) {
        try {
            return tutorService.deleteTutor(email);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/update")
    public ResponseEntity<TutorUser> updateTutor(@RequestBody UpdateTutorRequest updateTutorRequest) {
        try {
            return tutorService.updateTutor(updateTutorRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}