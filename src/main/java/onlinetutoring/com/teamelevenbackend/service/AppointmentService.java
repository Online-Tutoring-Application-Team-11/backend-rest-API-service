package onlinetutoring.com.teamelevenbackend.service;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import onlinetutoring.com.teamelevenbackend.controller.models.AppointmentRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.AppointmentsRecord;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.TutorsRecord;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.UsersRecord;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.AvailableHoursRecord;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import static onlinetutoring.com.teamelevenbackend.entity.tables.Appointments.APPOINTMENTS;
import static onlinetutoring.com.teamelevenbackend.entity.tables.AvailableHours.AVAILABLE_HOURS;
import static onlinetutoring.com.teamelevenbackend.entity.tables.Tutors.TUTORS;

@Component
public class AppointmentService {

    private DSLContext dslContext;
    private UserService userService;
    private EmailService emailService;
    @Autowired
    public void setAppointmentService(DSLContext dslContext, UserService userService, EmailService emailService) {
        this.userService = userService;
        this.dslContext = dslContext;
        this.emailService = emailService;
    }

    public ResponseEntity<List<Appointments>> listAppointmentByEmail(String email) throws SQLException {
        if (StringUtils.isEmpty(email)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            UsersRecord usersRecord = userService.get(email);

            Result<AppointmentsRecord> appointmentData = Boolean.TRUE.equals(usersRecord.getTutor())
                    ? dslContext.fetch(APPOINTMENTS, APPOINTMENTS.TUTOR_ID.eq(usersRecord.getId()))
                    : dslContext.fetch(APPOINTMENTS, APPOINTMENTS.STUDENT_ID.eq(usersRecord.getId()));

            if (appointmentData.isEmpty()) {
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
            }

            List<Appointments> response = new ArrayList<>();
            for (AppointmentsRecord app : appointmentData) {
                response.add(buildAppointment(app));
            }

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not query data", ex);
        }
    }

    public ResponseEntity<List<Appointments>> listAppointmentByEmail(String studentEmail, String tutorEmail) throws SQLException {
        if (StringUtils.isEmpty(studentEmail) || StringUtils.isEmpty(tutorEmail)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            UsersRecord usersRecordStu = userService.get(studentEmail);
            UsersRecord usersRecordTutor = userService.get(tutorEmail);

            Result<AppointmentsRecord> appointmentData = dslContext.fetch(APPOINTMENTS,
                    APPOINTMENTS.TUTOR_ID.eq(usersRecordTutor.getId()),
                    APPOINTMENTS.STUDENT_ID.eq(usersRecordStu.getId()));

            if (appointmentData.isEmpty()) {
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
            }

            List<Appointments> response = new ArrayList<>();
            for (AppointmentsRecord app : appointmentData) {
                response.add(buildAppointment(app));
            }

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not query data", ex);
        }
    }

    public ResponseEntity<Appointments> insertIntoAppointments(AppointmentRequest appointmentRequest) throws SQLException {
        if (StringUtils.isEmpty(appointmentRequest.getStudentEmail())
                || StringUtils.isEmpty(appointmentRequest.getTutorEmail())
                || StringUtils.isEmpty(appointmentRequest.getSubject())
                || appointmentRequest.getRequestedStartTime() == null
                || appointmentRequest.getRequestedEndTime() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            UsersRecord usersRecordStu = userService.get(appointmentRequest.getStudentEmail());
            UsersRecord usersRecordTutor = userService.get(appointmentRequest.getTutorEmail());

            if (!this.isTutorAvailableForAppointment(usersRecordTutor.getId(),
                    appointmentRequest.getRequestedStartTime(),
                    appointmentRequest.getRequestedEndTime())) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            dslContext.insertInto(APPOINTMENTS)
                    .set(APPOINTMENTS.TUTOR_ID, usersRecordTutor.getId())
                    .set(APPOINTMENTS.STUDENT_ID, usersRecordStu.getId())
                    .set(APPOINTMENTS.START_TIME, appointmentRequest.getRequestedStartTime())
                    .set(APPOINTMENTS.END_TIME, appointmentRequest.getRequestedEndTime())
                    .set(APPOINTMENTS.SUBJECT, appointmentRequest.getSubject())
                    .execute();

            // fetch the appointment
            Result<AppointmentsRecord> appointment = dslContext.fetch(APPOINTMENTS,
                    APPOINTMENTS.TUTOR_ID.eq(usersRecordTutor.getId()),
                    APPOINTMENTS.STUDENT_ID.eq(usersRecordStu.getId()),
                    APPOINTMENTS.SUBJECT.eq(appointmentRequest.getSubject()),
                    APPOINTMENTS.START_TIME.eq(appointmentRequest.getRequestedStartTime()));

            // update total hours for both student and tutor
            userService.updateTotalHours(usersRecordStu);
            userService.updateTotalHours(usersRecordTutor);

            // send confirmation emails
            emailService.sendConfirmationEmail(appointmentRequest.getStudentEmail(), appointmentRequest.getTutorEmail(), appointmentRequest.getSubject(), appointmentRequest.getRequestedStartTime());
            emailService.sendConfirmationEmail(appointmentRequest.getTutorEmail(), appointmentRequest.getStudentEmail(), appointmentRequest.getSubject(), appointmentRequest.getRequestedStartTime());

            return new ResponseEntity<>(buildAppointment(appointment.get(0)), HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not insert data into appointment", ex);
        }
    }

    public boolean isTutorAvailableForAppointment(int tutorId, LocalDateTime requestedStartTime, LocalDateTime requestedEndTime) {
        Result<TutorsRecord> tutorData = dslContext.fetch(TUTORS, TUTORS.ID.eq(tutorId));
        if (tutorData.isEmpty()) {
            return false;
        }

        DayOfWeek requestedDayOfWeek = requestedStartTime.getDayOfWeek();
        LocalTime startTime = requestedStartTime.toLocalTime();
        LocalTime endTime = requestedEndTime.toLocalTime();

        // Check if tutor is available on requested day
        Result<AvailableHoursRecord> availableHours = dslContext.fetch(AVAILABLE_HOURS,
                AVAILABLE_HOURS.TUTOR_ID.eq(tutorId),
                AVAILABLE_HOURS.DAY_OF_WEEK.eq(requestedDayOfWeek.toString().toLowerCase()));

        if (availableHours.isEmpty()) {
            return false;
        }

        Result<AppointmentsRecord> appointmentsRecords = dslContext.fetch(APPOINTMENTS, APPOINTMENTS.TUTOR_ID.eq(tutorId));

        for (AvailableHoursRecord availableHour : availableHours) {

            // Check if requested time is within tutor's working hours
            if ((startTime.isAfter(availableHour.getStartTime()) || startTime.equals(availableHour.getStartTime()))
                    && (endTime.isBefore(availableHour.getEndTime())) || startTime.equals(availableHour.getStartTime())) {

                // Check if tutor is available during requested time (not already booked)
                if (appointmentsRecords.isEmpty()) {
                    return true;
                }

                // Check if the tutor is previously booked
                for (AppointmentsRecord appointment : appointmentsRecords) {
                    // Appointment is completely before the booked time
                    if (((requestedStartTime.isBefore(appointment.getStartTime()))
                            && (requestedEndTime.isBefore(appointment.getStartTime())
                            || requestedEndTime.equals(appointment.getEndTime())))
                            // Appointment is completely after the booked time
                            || ((requestedStartTime.equals(appointment.getEndTime())
                            || requestedStartTime.isAfter(appointment.getEndTime()))
                            && requestedStartTime.isAfter(appointment.getEndTime()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ResponseEntity<HttpStatus> deleteAppointment(AppointmentRequest appointmentRequest) throws SQLException {
        if (StringUtils.isEmpty(appointmentRequest.getStudentEmail())
                || StringUtils.isEmpty(appointmentRequest.getTutorEmail())
                || StringUtils.isEmpty(appointmentRequest.getSubject())
                || appointmentRequest.getRequestedStartTime() == null
                || appointmentRequest.getRequestedEndTime() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            UsersRecord usersRecordStu = userService.get(appointmentRequest.getStudentEmail());
            UsersRecord usersRecordTutor = userService.get(appointmentRequest.getTutorEmail());

            Result<AppointmentsRecord> appointmentsRecords = dslContext.fetch(APPOINTMENTS,
                    APPOINTMENTS.TUTOR_ID.eq(usersRecordTutor.getId()),
                    APPOINTMENTS.STUDENT_ID.eq(usersRecordStu.getId()),
                    APPOINTMENTS.SUBJECT.eq(appointmentRequest.getSubject()),
                    APPOINTMENTS.START_TIME.eq(appointmentRequest.getRequestedStartTime()));

            if (appointmentsRecords.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // Cannot delete because the difference between current time and appointment start time
            // is less than 24 hours now
            Duration duration = Duration.between(LocalDateTime.now(), appointmentsRecords.get(0).getStartTime());
            if (duration.toHours() <= 24) {
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }

            // delete from table
            dslContext.deleteFrom(APPOINTMENTS)
                    .where(APPOINTMENTS.STUDENT_ID.eq(usersRecordStu.getId()))
                    .and(APPOINTMENTS.TUTOR_ID.eq(usersRecordTutor.getId()))
                    .and(APPOINTMENTS.SUBJECT.eq(appointmentRequest.getSubject()))
                    .and(APPOINTMENTS.START_TIME.eq(appointmentRequest.getRequestedStartTime()))
                    .and(APPOINTMENTS.END_TIME.eq(appointmentRequest.getRequestedEndTime()))
                    .execute();

            // update total hours for both student and tutor
            userService.updateTotalHours(usersRecordStu);
            userService.updateTotalHours(usersRecordTutor);

            // send cancellation emails
            emailService.sendCancellationEmail(appointmentRequest.getStudentEmail(), appointmentRequest.getTutorEmail(), appointmentRequest.getSubject(), appointmentRequest.getRequestedStartTime());
            emailService.sendCancellationEmail(appointmentRequest.getTutorEmail(), appointmentRequest.getStudentEmail(), appointmentRequest.getSubject(), appointmentRequest.getRequestedStartTime());

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not delete Appointment", ex);
        }
    }

    private static Appointments buildAppointment(AppointmentsRecord appointmentsRecord) {
        Appointments response = new Appointments();

        response.setTutorId(appointmentsRecord.getTutorId());
        response.setStudentId(appointmentsRecord.getStudentId());
        response.setStartTime(appointmentsRecord.getStartTime());
        response.setEndTime(appointmentsRecord.getEndTime());
        response.setSubject(appointmentsRecord.getSubject());

        return response;
    }
}
