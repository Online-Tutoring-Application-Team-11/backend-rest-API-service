package onlinetutoring.com.teamelevenbackend.controller;

import onlinetutoring.com.teamelevenbackend.controller.models.AppointmentRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments;
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

/**
 * Controller class that manages appointments.
 * Contains endpoints for retrieving, creating, and deleting appointments.
 */
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

    /**
     * Retrieves a list of appointments for a given email address.
     *
     * @param email  The email address for which to retrieve the list of appointments.
     * @return ResponseEntity   Contains a list of appointments.
     * @throws Exception   Appointment is not found.
     */
    @GetMapping(value = "/list/{email}")
    public ResponseEntity<List<Appointments>> listAppointmentsByEmail(@PathVariable("email") String email) {
        try {
            return appointmentService.listAppointmentByEmail(email);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieves a list of appointments for a given student and tutor email addresses.
     *
     * @param studentEmail  The email address of the student for which to retrieve the list of appointments.
     * @param tutorEmail    The email address of the tutor for which to retrieve the list of appointments.
     * @return: ResponseEntity  Contains a list of appointments.
     * @throws Exception   Couldn't query the appointment data.
     */
    @GetMapping(value = "/list")
    public ResponseEntity<List<Appointments>> getAppointment(@RequestParam String studentEmail,
                                                             @RequestParam String tutorEmail) {
        try {
            return appointmentService.listAppointmentByEmail(studentEmail, tutorEmail);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Creates a new appointment based on the provided appointment request.
     *
     * @param appointmentRequest   The appointment request object containing the information for creating a new appointment.
     * @return: a ResponseEntity  Contains the created appointment.
     * @throws Exception   Couldn't create the appointment.
     */
    @PostMapping(value = "/create")
    public ResponseEntity<Appointments> insertIntoAppointment(@RequestBody AppointmentRequest appointmentRequest) {
        try {
            return appointmentService.insertIntoAppointments(appointmentRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Deletes an appointment based on the provided appointment request.
     *
     * @param appointmentRequest  The appointment request object containing the information for deleting an appointment
     * @return: ResponseEntity   Contains the status of the delete operation
     * @throws Exception   Couldn't delete the appointment.
     */
    @DeleteMapping(value = "/delete")
    public ResponseEntity<HttpStatus> deleteAppointment(@RequestBody AppointmentRequest appointmentRequest) {
        try {
            return appointmentService.deleteAppointment(appointmentRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
