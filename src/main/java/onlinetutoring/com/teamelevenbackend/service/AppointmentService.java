package onlinetutoring.com.teamelevenbackend.service;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import onlinetutoring.com.teamelevenbackend.controller.models.UpdateAppointmentRequest;
import onlinetutoring.com.teamelevenbackend.controller.models.UpdateStudentRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.*;
import onlinetutoring.com.teamelevenbackend.models.StudentUser;
import onlinetutoring.com.teamelevenbackend.models.TutorUser;
import onlinetutoring.com.teamelevenbackend.models.enums.Days;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static onlinetutoring.com.teamelevenbackend.entity.Tables.*;
import static onlinetutoring.com.teamelevenbackend.entity.Tables.STUDENTS;
import static onlinetutoring.com.teamelevenbackend.entity.tables.Tutors.TUTORS;

@Service
public class AppointmentService {

    private DSLContext dslContext;
    private AuthService authService;

    @Autowired
    public void setDslContext(DSLContext dslContext) {
        this.dslContext = dslContext;
    }


    public ResponseEntity<AppointmentsRecord> getAppointmentByTutorEmail(String email) throws SQLException {
        if (StringUtils.isEmpty(email)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> userData = dslContext.fetch(USERS, USERS.EMAIL.eq(email));
            if (userData.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            UsersRecord usersRecord = userData.get(0);

            Result<AppointmentsRecord> appointmentData = dslContext.fetch(APPOINTMENTS, APPOINTMENTS.TUTOR_ID.eq(usersRecord.getId()));
            if (appointmentData.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }


            return new ResponseEntity<>(this.buildAppointment(appointmentData.get(0)), HttpStatus.OK);

        } catch (Exception ex) {
            throw new SQLException("Could not query data", ex);
        }
    }


    public boolean insertIntoAppointments(int tutorId, int studentId, LocalDateTime requestedStartTime,
                                          LocalDateTime requestedEndTime, String subject) throws SQLException {
        // TO DO Check if TutorId is empty
        if (Objects.isNull(requestedStartTime)
                || Objects.isNull(requestedEndTime)
                || StringUtils.isEmpty(subject))
            return false;

        try {
            if (!this.isTutorAvailableForAppointment(tutorId, requestedStartTime, requestedEndTime)) {
                return false;
            }

            dslContext.insertInto(APPOINTMENTS)
                    .set(APPOINTMENTS.TUTOR_ID, tutorId)
                    .set(APPOINTMENTS.STUDENT_ID, studentId)
                    .set(APPOINTMENTS.START_TIME, requestedStartTime)
                    .set(APPOINTMENTS.END_TIME, requestedEndTime)
                    .set(APPOINTMENTS.SUBJECT, subject)
                    .execute();


            Result<AppointmentsRecord> appointment = dslContext.fetch(APPOINTMENTS, APPOINTMENTS.TUTOR_ID.eq(tutorId));

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

    public ResponseEntity<AppointmentsRecord> updateAppointment(UpdateAppointmentRequest updateAppointmentRequest) throws SQLException {
        if (StringUtils.isEmpty(updateAppointmentRequest.getEmail())
                || Objects.isNull(updateAppointmentRequest.getRequestedStartTime())
                || Objects.isNull(updateAppointmentRequest.getRequestedEndTime())
                || StringUtils.isEmpty(updateAppointmentRequest.getSubject())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> userData = dslContext.fetch(USERS, USERS.EMAIL.eq(updateAppointmentRequest.getEmail()));
            if (userData.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            UsersRecord usersRecord = userData.get(0);

            Result<AppointmentsRecord> appointment = dslContext.fetch(APPOINTMENTS, APPOINTMENTS.STUDENT_ID.eq(usersRecord.getId()));
            if (appointment.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            AppointmentsRecord appointmentData = appointment.get(0);

            // update appointments
            dslContext.update(APPOINTMENTS)
                    .set(APPOINTMENTS.SUBJECT, updateAppointmentRequest.getSubject())
                    .set(APPOINTMENTS.START_TIME, updateAppointmentRequest.getRequestedStartTime())
                    .set(APPOINTMENTS.END_TIME, updateAppointmentRequest.getRequestedEndTime())
                    .where(APPOINTMENTS.STUDENT_ID.eq(appointmentData.getStudentId()))
                    .execute();


            AppointmentsRecord resAppointment = dslContext.fetch(APPOINTMENTS, APPOINTMENTS.STUDENT_ID.eq(appointmentData.getStudentId())).get(0);

            return new ResponseEntity<>(this.buildAppointment(resAppointment), HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not update appointment", ex);
        }
    }

    private AppointmentsRecord buildAppointment(AppointmentsRecord appointmentsRecord) {
        AppointmentsRecord response = new AppointmentsRecord();

        // appointment data
        response.setTutorId(appointmentsRecord.getTutorId());
        response.setStudentId(appointmentsRecord.getStudentId());
        response.setStartTime(appointmentsRecord.getStartTime());
        response.setEndTime(appointmentsRecord.getEndTime());
        response.setSubject(appointmentsRecord.getSubject());
        return response;
    }

    //Delete Appointment
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
}
