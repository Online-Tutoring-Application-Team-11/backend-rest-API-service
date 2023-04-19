package onlinetutoring.com.teamelevenbackend.service;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import onlinetutoring.com.teamelevenbackend.controller.models.CreateAppointmentRequest;
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
import static onlinetutoring.com.teamelevenbackend.entity.tables.Users.USERS;

@Component
public class AppointmentService {

    private DSLContext dslContext;

    @Autowired
    public void setDslContext(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public ResponseEntity<AppointmentsRecord> getAppointmentByEmail(String email) throws SQLException {
        if (StringUtils.isEmpty(email)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> userData = dslContext.fetch(USERS, USERS.EMAIL.eq(email));
            if (userData.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            UsersRecord usersRecord = userData.get(0);

            Result<AppointmentsRecord> appointmentData = Boolean.TRUE.equals(usersRecord.getTutor())
                    ? dslContext.fetch(APPOINTMENTS, APPOINTMENTS.TUTOR_ID.eq(usersRecord.getId()))
                    : dslContext.fetch(APPOINTMENTS, APPOINTMENTS.STUDENT_ID.eq(usersRecord.getId()));

            if (appointmentData.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(buildAppointment(appointmentData.get(0)), HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not query data", ex);
        }
    }

    public ResponseEntity<AppointmentsRecord> getAppointmentByEmail(String studentEmail, String tutorEmail) throws SQLException {
        if (StringUtils.isEmpty(studentEmail) || StringUtils.isEmpty(tutorEmail)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> userDataStudent = dslContext.fetch(USERS, USERS.EMAIL.eq(studentEmail));
            if (userDataStudent.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            UsersRecord usersRecordStu = userDataStudent.get(0);

            Result<UsersRecord> userDataTutor = dslContext.fetch(USERS, USERS.EMAIL.eq(tutorEmail));
            if (userDataTutor.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            UsersRecord usersRecordTutor = userDataTutor.get(0);

            Result<AppointmentsRecord> appointmentData = dslContext.fetch(APPOINTMENTS,
                    APPOINTMENTS.TUTOR_ID.eq(usersRecordTutor.getId()),
                    APPOINTMENTS.STUDENT_ID.eq(usersRecordStu.getId()));

            if (appointmentData.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(buildAppointment(appointmentData.get(0)), HttpStatus.OK);

        } catch (Exception ex) {
            throw new SQLException("Could not query data", ex);
        }
    }

    public ResponseEntity<AppointmentsRecord> insertIntoAppointments(CreateAppointmentRequest createAppointmentRequest) throws SQLException {
        if (StringUtils.isEmpty(createAppointmentRequest.getStudentEmail())
                || StringUtils.isEmpty(createAppointmentRequest.getTutorEmail())
                || StringUtils.isEmpty(createAppointmentRequest.getSubject())
                || createAppointmentRequest.getRequestedStartTime() == null
                || createAppointmentRequest.getRequestedEndTime() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> userDataStudent = dslContext.fetch(USERS, USERS.EMAIL.eq(studentEmail));
            if (userDataStudent.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            UsersRecord usersRecordStu = userDataStudent.get(0);

            Result<UsersRecord> userDataTutor = dslContext.fetch(USERS, USERS.EMAIL.eq(tutorEmail));
            if (userDataTutor.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            UsersRecord usersRecordTutor = userDataTutor.get(0);

            if (!this.isTutorAvailableForAppointment(createAppointmentRequest.getTutorEmail(),
                    createAppointmentRequest.getRequestedStartTime(),
                    createAppointmentRequest.getRequestedStartTime())) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            dslContext.insertInto(APPOINTMENTS)
                    .set(APPOINTMENTS.TUTOR_ID, tutorId)
                    .set(APPOINTMENTS.STUDENT_ID, studentId)
                    .set(APPOINTMENTS.START_TIME, requestedStartTime)
                    .set(APPOINTMENTS.END_TIME, requestedEndTime)
                    .set(APPOINTMENTS.SUBJECT, subject)
                    .execute();


            Result<AppointmentsRecord> appointment = dslContext.fetch(APPOINTMENTS,
                    APPOINTMENTS.TUTOR_ID.eq(tutorId));

            // check if insert failed
            return !appointment.isEmpty();
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

    public ResponseEntity<HttpStatus> deleteAppointment(String email, LocalDateTime requestedStartTime,
                                                        LocalDateTime requestedEndTime) throws SQLException {
        if (StringUtils.isEmpty(email)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> resUser = dslContext.fetch(USERS, USERS.EMAIL.eq(email));

            // user does not exists
            if (resUser.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }


            UsersRecord user = resUser.get(0);

            Result<AppointmentsRecord> appointmentsRecords = dslContext.fetch(APPOINTMENTS, APPOINTMENTS.STUDENT_ID.eq(user.getId()));

            // if the day path param is present
            if ((requestedStartTime != null) && (requestedEndTime != null)) {
                appointmentsRecords = dslContext.fetch(APPOINTMENTS, APPOINTMENTS.START_TIME.eq(requestedStartTime),
                        APPOINTMENTS.END_TIME.eq(requestedEndTime));
            }

            if (appointmentsRecords.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // delete from table
            dslContext.deleteFrom(APPOINTMENTS)
                    .where(APPOINTMENTS.STUDENT_ID.eq(user.getId()))
                    .and(APPOINTMENTS.START_TIME.eq(requestedStartTime))
                    .and(APPOINTMENTS.END_TIME.eq(requestedEndTime))
                    .execute();


            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not delete Appointment", ex);
        }
    }

    private static AppointmentsRecord buildAppointment(AppointmentsRecord appointmentsRecord) {
        AppointmentsRecord response = new AppointmentsRecord();

        // appointment data
        response.setTutorId(appointmentsRecord.getTutorId());
        response.setStudentId(appointmentsRecord.getStudentId());
        response.setStartTime(appointmentsRecord.getStartTime());
        response.setEndTime(appointmentsRecord.getEndTime());
        response.setSubject(appointmentsRecord.getSubject());
        return response;
    }
}
