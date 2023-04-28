package onlinetutoring.com.teamelevenbackend.controller;

import onlinetutoring.com.teamelevenbackend.controller.models.AppointmentRequest;
import onlinetutoring.com.teamelevenbackend.controller.models.AppointmentResponse;
import onlinetutoring.com.teamelevenbackend.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ResponseEntity<List<AppointmentResponse>> listAppointmentsByEmail(@PathVariable("email") String email) {
        try {
            return appointmentService.listAppointmentByEmail(email);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/list")
    public ResponseEntity<List<AppointmentResponse>> getAppointment(@RequestParam String studentEmail,
                                                             @RequestParam String tutorEmail) {
        try {
            return appointmentService.listAppointmentByEmail(studentEmail, tutorEmail);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/create")
    public ResponseEntity<AppointmentResponse> insertIntoAppointment(@RequestBody AppointmentRequest appointmentRequest) {
        try {
            return appointmentService.insertIntoAppointments(appointmentRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/delete")
    public ResponseEntity<HttpStatus> deleteAppointment(@RequestBody AppointmentRequest appointmentRequest) {
        try {
            return appointmentService.deleteAppointment(appointmentRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
