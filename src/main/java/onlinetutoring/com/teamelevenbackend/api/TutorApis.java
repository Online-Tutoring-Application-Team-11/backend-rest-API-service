package onlinetutoring.com.teamelevenbackend.api;

import onlinetutoring.com.teamelevenbackend.models.TutorUser;
import onlinetutoring.com.teamelevenbackend.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@RestController
@RequestMapping("/tutors")
public class TutorApis {

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
}