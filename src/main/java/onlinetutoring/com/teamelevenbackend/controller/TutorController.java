package onlinetutoring.com.teamelevenbackend.controller;

import onlinetutoring.com.teamelevenbackend.controller.models.ModifyAvailableHours;
import onlinetutoring.com.teamelevenbackend.controller.models.UpdateTutorRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.AvailableHours;
import onlinetutoring.com.teamelevenbackend.models.TutorUser;
import onlinetutoring.com.teamelevenbackend.models.enums.Days;
import onlinetutoring.com.teamelevenbackend.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static onlinetutoring.com.teamelevenbackend.controller.ControllerUtils.BASE_LOCAL;
import static onlinetutoring.com.teamelevenbackend.controller.ControllerUtils.BASE_PRODUCTION;

@Controller
@RestController
@RequestMapping("/tutors")
@CrossOrigin(origins = {BASE_LOCAL}, methods = {RequestMethod.PUT, RequestMethod.DELETE})
public class TutorController {

    private TutorService tutorService;
    @Autowired
    public void setTutorService(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    @GetMapping(value = "/get/all")
    public ResponseEntity<List<TutorUser>> getTutors(@RequestParam(required = false) String subject) {
        try {
            return tutorService.getAllTutors(subject);
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

    @PutMapping(value = "/update")
    public ResponseEntity<TutorUser> updateTutor(@RequestBody UpdateTutorRequest updateTutorRequest) {
        try {
            return tutorService.updateTutor(updateTutorRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get/{email}/available-hours")
    public ResponseEntity<List<AvailableHours>> getAvailableHours(@PathVariable("email") String email) {
        try {
            return tutorService.getAvailableHours(email);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/available-hours/modify")
    public ResponseEntity<List<AvailableHours>> modifyAvailableHours(@RequestBody ModifyAvailableHours modifyAvailableHours) {
        try {
            return tutorService.modifyAvailableHours(modifyAvailableHours);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/available-hours/{email}/delete")
    public ResponseEntity<HttpStatus> deleteAvailableHours(@PathVariable("email") String email,
                                                           @RequestParam(required = false) Days day) {
        try {
            return tutorService.deleteAvailableHours(email, day);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}