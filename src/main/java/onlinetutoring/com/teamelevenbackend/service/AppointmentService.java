package onlinetutoring.com.teamelevenbackend.service;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import onlinetutoring.com.teamelevenbackend.controller.models.AppointmentRequest;
import onlinetutoring.com.teamelevenbackend.controller.models.AppointmentResponse;
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

    // Only for internal use by scheduled jobs
    private static final ZoneId UTC = ZoneId.of("UTC");

    private DSLContext dslContext;
    private UserService userService;
    private EmailService emailService;
    @Autowired
    public void setAppointmentService(DSLContext dslContext, UserService userService, EmailService emailService) {
        this.userService = userService;
        this.dslContext = dslContext;
        this.emailService = emailService;
    }

    /**
     * Get Appointments using the email of user
     *
     * @param email The email of the user
     * @return The appointments associated with the email
     * @throws SQLException Couldn't query the data
     */
    public ResponseEntity<List<AppointmentResponse>> listAppointmentByEmail(String email) throws SQLException {
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

            List<AppointmentResponse> response = new ArrayList<>();
            for (AppointmentsRecord app : appointmentData) {
                response.add(buildAppointmentResponse(app));
            }

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not query data", ex);
        }
    }

    /**
     * Get the appointments using the emails of the users
     *
     * @param studentEmail The email of the student
     * @param tutorEmail The email of the tutor
     * @return The appointments of the users
     * @throws SQLException Couldn't query the data
     */
    public ResponseEntity<List<AppointmentResponse>> listAppointmentByEmail(String studentEmail, String tutorEmail) throws SQLException {
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

            List<AppointmentResponse> response = new ArrayList<>();
            for (AppointmentsRecord app : appointmentData) {
                response.add(buildAppointmentResponse(app));
            }

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not query data", ex);
        }
    }

    /**
     * Insertion into appointments using the AppointmentRequest
     *
     * @param appointmentRequest The appointment request
     * @return The resulting appointment response
     * @throws SQLException The data couldn't be inserted into appointment
     */
    public ResponseEntity<AppointmentResponse> insertIntoAppointments(AppointmentRequest appointmentRequest) throws SQLException {
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

            return new ResponseEntity<>(buildAppointmentResponse(appointment.get(0)), HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not insert data into appointment", ex);
        }
    }

    /**
     * Checking if the tutor is available for an appointment
     *
     * @param tutorId The id of the tutor
     * @param requestedStartTime The requested start time
     * @param requestedEndTime The requested end time
     * @return The result whether the tutor is available
     */
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

    /**
     * Deleting the appointment using AppointmentRequest
     *
     * @param appointmentRequest The appointment request to delete
     * @return The status of the action
     * @throws SQLException The appointment couldn't be deleted
     */
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


    public List<AppointmentResponse> getReminderAppointments() {

        // create a LocalDateTime object representing 15 minutes from now
        LocalDateTime fifteenMinutesFromNow = ZonedDateTime.now(UTC).toLocalDateTime().plus(15, ChronoUnit.MINUTES);

        // create a LocalDateTime object representing 16 minutes from now
        LocalDateTime sixteenMinutesFromNow = ZonedDateTime.now(UTC).toLocalDateTime().plus(16, ChronoUnit.MINUTES);

        List<AppointmentResponse> emailList = new ArrayList<>();

        try {
            Result<AppointmentsRecord> appointmentsRecords = dslContext.fetch(APPOINTMENTS,
                    APPOINTMENTS.START_TIME.between(fifteenMinutesFromNow, sixteenMinutesFromNow));

            for (AppointmentsRecord app : appointmentsRecords) {
                emailList.add(buildAppointmentResponse(app));
            }

            return emailList;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    /**
     * The cleanup of the appointments
     */
    public void cleanup() {
        try {
            // delete from table
            dslContext.deleteFrom(APPOINTMENTS)
                    .where(APPOINTMENTS.END_TIME.le(ZonedDateTime.now(UTC).toLocalDateTime()))
                    .and(APPOINTMENTS.TUTOR_ID.le(0)) // this is a bug introduced till we can find a fix for calculating total-hours in a better way
                    .execute();
        } catch (Exception ignored) {}
    }

    /**
     *  Build the appointment response using the appointmentsRecord
     *
     * @param appointmentsRecord The appointments record
     * @return The appointments response
     */
    private AppointmentResponse buildAppointmentResponse(AppointmentsRecord appointmentsRecord) {
        AppointmentResponse response = new AppointmentResponse();

        response.setTutorId(appointmentsRecord.getTutorId());
        response.setStudentId(appointmentsRecord.getStudentId());
        response.setStartTime(appointmentsRecord.getStartTime());
        response.setEndTime(appointmentsRecord.getEndTime());
        response.setSubject(appointmentsRecord.getSubject());
        response.setTutorEmail(userService.getEmailById(appointmentsRecord.getTutorId()));
        response.setStudentEmail(userService.getEmailById(appointmentsRecord.getStudentId()));

        return response;
    }
}
