package onlinetutoring.com.teamelevenbackend.api;
import onlinetutoring.com.teamelevenbackend.api.models.UpdateTutorRequest;
import onlinetutoring.com.teamelevenbackend.service.TutorService;
import onlinetutoring.com.teamelevenbackend.models.TutorUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RestController
@RequestMapping("/api/tutor")
public class TutorApis {

    @Autowired
    private TutorService tutorService;
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
