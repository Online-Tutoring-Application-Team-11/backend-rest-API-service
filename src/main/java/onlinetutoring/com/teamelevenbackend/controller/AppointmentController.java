package onlinetutoring.com.teamelevenbackend.controller;

import onlinetutoring.com.teamelevenbackend.controller.models.CreateAppointmentRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.AppointmentsRecord;
import onlinetutoring.com.teamelevenbackend.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static onlinetutoring.com.teamelevenbackend.utils.ControllerUtils.BASE_LOCAL;
import static onlinetutoring.com.teamelevenbackend.utils.ControllerUtils.BASE_PRODUCTION;

@Controller
@RestController
@RequestMapping("/appointments")
@CrossOrigin(origins = {BASE_PRODUCTION, BASE_LOCAL}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
public class AppointmentController {

    private AppointmentService appointmentService;

    @Autowired
    public void setAppointmentService(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping(value = "/list/{email}")
    public ResponseEntity<AppointmentsRecord> listAppointmentsByEmail(@PathVariable("email") String email) {
        try {
            return appointmentService.getAppointmentByEmail(email);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/get")
    public ResponseEntity<AppointmentsRecord> getAppointment(@RequestParam String studentEmail,
                                                             @RequestParam String tutorEmail) {
        try {
            return appointmentService.getAppointmentByEmail(studentEmail, tutorEmail);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/insert")
    public ResponseEntity<AppointmentsRecord> insertIntoAppointment(@RequestBody CreateAppointmentRequest createAppointmentRequest) {
        try {
            return appointmentService.insertIntoAppointments(createAppointmentRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/appointment/{email}/delete")
    public ResponseEntity<HttpStatus> deleteAppointment(@PathVariable("email") String email,
                                                        @RequestParam(required = false)
                                                        LocalDateTime requestedStartTime, LocalDateTime requestedEndTime) {
        try {
            return appointmentService.deleteAppointment(email, requestedStartTime, requestedEndTime);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


}
