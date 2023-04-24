package onlinetutoring.com.teamelevenbackend.service;

import java.sql.SQLException;
import java.time.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import javax.mail.Session;
import javax.mail.Transport;

import onlinetutoring.com.teamelevenbackend.controller.models.AppointmentRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.*;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import static onlinetutoring.com.teamelevenbackend.entity.Tables.STUDENTS;
import static onlinetutoring.com.teamelevenbackend.entity.Tables.USERS;
import static onlinetutoring.com.teamelevenbackend.entity.tables.Appointments.APPOINTMENTS;
import static onlinetutoring.com.teamelevenbackend.entity.tables.AvailableHours.AVAILABLE_HOURS;
import static onlinetutoring.com.teamelevenbackend.entity.tables.Tutors.TUTORS;

@Component
public class AppointmentService {

    private DSLContext dslContext;
    private UserService userService;
    @Autowired
    public void setAppointmentService(DSLContext dslContext, UserService userService) {
        this.userService = userService;
        this.dslContext = dslContext;
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

            // Send confirmation email to the student

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

    // Sends confirmation email to the student
    private void sendConfirmationEmail(int studentId, LocalDateTime startTime, String subject) {
        // Get student's email address from the database using studentId
        String studentEmail = getEmailAddress(studentId);

        // Construct email content
        String emailSubject = "Appointment confirmation for " + subject;
        String emailBody = "Dear student,\n\nYour appointment for " + subject + " is confirmed for " +
                startTime.toString() + ".\n\nPlease contact us if you have any questions or need to reschedule.\n\n" +
                "Best regards,\nA+ Online Tutoring";


        // Send email
        try {
            sendEmail(studentEmail, emailSubject, emailBody);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    // Sends reminder email to the student 15 minutes before the appointment
    private void sendReminderEmail(int studentId, LocalDateTime requestedStartTime, String subject) {

        String studentEmail = getEmailAddress(studentId);

        // Calculate the appointment start time, 15 minutes before the actual start time
        LocalDateTime reminderTime = requestedStartTime.minusMinutes(15);

        // Construct email content
        String emailSubject = "Appointment reminder for " + subject;
        String emailBody = "Dear student,\n\nThis is a reminder that your appointment for " + subject +
                " is scheduled to start in 15 minutes, at " + requestedStartTime.toString() + ".\n\nPlease be on time and " +
                "ready for the appointment.\n\nBest regards,\nA+ Online Tutoring";

        // Send email
        TimerTask reminderTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    sendEmail(studentEmail, emailSubject, emailBody);
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }

            }
        };
        Timer reminderTimer = new Timer();
        reminderTimer.schedule(reminderTask, Date.from(reminderTime.atZone(ZoneId.systemDefault()).toInstant()));


    }

    private String getEmailAddress(int id) {
        Result<UsersRecord>userRecords = dslContext.fetch(USERS,USERS.ID.eq(id));
        return userRecords.get(0).getEmail();
    }

    // Send an email using a third-party email service
    public static void sendEmail(String studentEmail, String emailSubject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
// We need to create gmail account, eg A+Tutoring@gmail.com
        String senderEmail = "-----@gmail.com";
        String senderPassword = "test1234";

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(studentEmail));
        message.setSubject(emailSubject);
        message.setText(body);

        Transport.send(message);
    }

}
