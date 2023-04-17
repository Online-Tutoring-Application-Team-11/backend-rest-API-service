package onlinetutoring.com.teamelevenbackend.controller;

import onlinetutoring.com.teamelevenbackend.controller.models.UpdateAppointmentRequest;
import onlinetutoring.com.teamelevenbackend.controller.models.UpdateStudentRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.AppointmentsRecord;
import onlinetutoring.com.teamelevenbackend.models.StudentUser;
import onlinetutoring.com.teamelevenbackend.models.enums.Days;
import onlinetutoring.com.teamelevenbackend.service.AppointmentService;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static onlinetutoring.com.teamelevenbackend.entity.tables.Appointments.APPOINTMENTS;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private AppointmentService appointmentService;
    private DSLContext dslContext;

    @Autowired
    public void setAppointmentService(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping(value = "/get/{email}")
    public ResponseEntity<AppointmentsRecord> getAppointment(@PathVariable("email") String email) {
        try {
            return appointmentService.getAppointmentByTutorEmail(email);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/insert")
    public boolean insertIntoAppointment(int tutorId, int studentId, LocalDateTime requestedStartTime,
                                         LocalDateTime requestedEndTime, String subject) {
        try {
            return appointmentService.insertIntoAppointments(tutorId, studentId, requestedStartTime,
                    requestedEndTime, subject);
        } catch (Exception ex) {
            return false;
        }
    }

    @PutMapping(value = "/update")
    public ResponseEntity<AppointmentsRecord> updateAppointment(@RequestBody UpdateAppointmentRequest updateAppointmentRequest) {
        try {
            return appointmentService.updateAppointment(updateAppointmentRequest);
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
