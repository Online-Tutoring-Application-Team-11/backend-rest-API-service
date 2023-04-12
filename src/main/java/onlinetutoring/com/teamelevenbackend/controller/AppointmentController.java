package onlinetutoring.com.teamelevenbackend.controller;

import onlinetutoring.com.teamelevenbackend.entity.tables.records.AppointmentsRecord;
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

    @PostMapping
    public ResponseEntity<String> createAppointment(@RequestParam int tutorId,int studentId,
                                                    @RequestParam LocalDateTime startTime,
                                                    @RequestParam LocalDateTime endTime) {
        if (appointmentService.isTutorAvailableForAppointment(tutorId, startTime, endTime)) {
            // appointment can be created
            AppointmentsRecord newAppointment = dslContext.newRecord(APPOINTMENTS);
            newAppointment.setTutorId(tutorId);
            newAppointment.setStudentId(studentId);
            newAppointment.setStartTime(startTime);
            newAppointment.setEndTime(endTime);
            newAppointment.store();
            return new ResponseEntity<>("Appointment created successfully", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Tutor is not available for the requested time", HttpStatus.BAD_REQUEST);
        }
    }

}
