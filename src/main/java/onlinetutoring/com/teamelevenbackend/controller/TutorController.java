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

import java.time.LocalTime;
import java.util.List;

import static onlinetutoring.com.teamelevenbackend.utils.ControllerUtils.BASE_LOCAL;
import static onlinetutoring.com.teamelevenbackend.utils.ControllerUtils.BASE_PRODUCTION;

/**
 * Controller class for handling requests related to tutors.
 */
@Controller
@RestController
@RequestMapping("/tutors")
@CrossOrigin(origins = {BASE_PRODUCTION, BASE_LOCAL}, methods = {RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE})
public class TutorController {

    private TutorService tutorService;
    @Autowired
    public void setTutorService(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    /**
     * Retrieves a list of all tutors or tutors with a provided subject.
     *
     * @param subject   The subject (optional) for which to retrieve tutors.
     * @return ResponseEntity  Contains a list of TutorUser objects.
     * @throws Exception   Couldn't retrieve tutors.
     */
    @GetMapping(value = "/get/all")
    public ResponseEntity<List<TutorUser>> getTutors(@RequestParam(required = false) String subject) {
        try {
            return tutorService.getAllTutors(subject);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieves tutor information by email address.
     *
     * @param email The email address of the tutor to retrieve.
     * @return ResponseEntity    Contains a TutorUser object.
     * @throws Exception    Couldn't retrieve tutor information from the database.
     */
    @GetMapping(value = "/get/{email}")
    public ResponseEntity<TutorUser> getTutor(@PathVariable(value = "email", required = false) String email) {
        try{
            return tutorService.getTutorByEmail(email);
        }catch (Exception ex){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Updates tutor information.
     *
     * @param updateTutorRequest  A request object containing the updated tutor information.
     * @return ResponseEntity    Contains updated TutorUser object.
     * @throws Exception     Couldn't update tutor in the database.
     */
    @PutMapping(value = "/update")
    public ResponseEntity<TutorUser> updateTutor(@RequestBody UpdateTutorRequest updateTutorRequest) {
        try {
            return tutorService.updateTutor(updateTutorRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieves a tutor's available hours by email address.
     *
     * @param email  The email address of the tutor for which to retrieve available hours.
     * @return ResponseEntity   Contains a list of AvailableHours objects.
     * @throws Exception    Couldn't retrieve available hours information from the database.
     */
    @GetMapping(value = "/get/{email}/available-hours")
    public ResponseEntity<List<AvailableHours>> getAvailableHours(@PathVariable(value = "email", required = false) String email) {
        try {
            return tutorService.getAvailableHours(email);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Modifies the available hours for a tutor.
     *
     * @param modifyAvailableHours  Object containing the tutor's email and the modified available hours
     * @return ResponseEntity   Contains a list of the tutor's modified available hours.
     * @throws Exception    Couldn't  modify the available hours.
     */
    @PutMapping(value = "/available-hours/modify")
    public ResponseEntity<List<AvailableHours>> modifyAvailableHours(@RequestBody ModifyAvailableHours modifyAvailableHours) {
        try {
            return tutorService.modifyAvailableHours(modifyAvailableHours);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Deletes a tutor's available hours for a specific day and start time.
     *
     * @param email  The tutor's email.
     * @param day   The day of the week for which the available hours should be deleted (optional)
     * @param startTime  The start time for which the available hours should be deleted (optional)
     * @return   ResponseEntity  Empty content to show deletion.
     * @throws Exception    Couldn't delete available hours.
     */
    @DeleteMapping(value = "/available-hours/{email}/delete")
    public ResponseEntity<HttpStatus> deleteAvailableHours(@PathVariable(value = "email", required = false) String email,
                                                           @RequestParam(required = false) Days day,
                                                           @RequestParam(required = false) LocalTime startTime) {
        try {
            return tutorService.deleteAvailableHours(email, day, startTime);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}