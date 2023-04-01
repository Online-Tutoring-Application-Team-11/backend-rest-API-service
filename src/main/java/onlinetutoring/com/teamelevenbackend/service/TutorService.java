package onlinetutoring.com.teamelevenbackend.service;

import onlinetutoring.com.teamelevenbackend.controller.models.ModifyAvailableHours;
import onlinetutoring.com.teamelevenbackend.controller.models.UpdateTutorRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.AvailableHours;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.AvailableHoursRecord;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.TutorsRecord;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.UsersRecord;
import onlinetutoring.com.teamelevenbackend.models.TutorUser;
import onlinetutoring.com.teamelevenbackend.models.enums.Days;
import org.apache.commons.collections4.CollectionUtils;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static onlinetutoring.com.teamelevenbackend.entity.Tables.AVAILABLE_HOURS;
import static onlinetutoring.com.teamelevenbackend.entity.Tables.USERS;
import static onlinetutoring.com.teamelevenbackend.entity.Tables.TUTORS;

@Component
public class TutorService {

    private DSLContext dslContext;
    @Autowired
    public void setDslContext(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public ResponseEntity<List<TutorUser>> getAllTutors(String subject) throws SQLException {
        try {
            Result<TutorsRecord> allTutors;
            if (StringUtils.isEmpty(subject)) {
                // fetching all tutors
                allTutors = dslContext.fetch(TUTORS, TUTORS.ID.ge(0));
            } else {
                // fetching all tutors by a subject
                allTutors = dslContext.fetch(TUTORS, TUTORS.SUBJECTS.contains((new String[] {subject})));
            }

            List<TutorUser> response = new ArrayList<>();

            for (TutorsRecord tutor : allTutors) {
                UsersRecord resUser = dslContext.fetch(USERS, USERS.ID.eq(tutor.getId())).get(0);
                response.add(buildTutorUser(resUser, tutor));
            }

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not fetch all tutors", ex);
        }
    }

    public List<Integer> validateIsTutor(List<Integer> tutorIds) {
        if (CollectionUtils.isEmpty(tutorIds)) {
            return Collections.emptyList();
        }

        List<Integer> finalTutorList = new ArrayList<>();
        for (Integer id : tutorIds) {
            Result<TutorsRecord> tutorData = dslContext.fetch(TUTORS, TUTORS.ID.eq(id));
            if (tutorData.isNotEmpty()) {
                finalTutorList.add(tutorData.get(0).getId());
            }
        }

        return finalTutorList;
    }

    public boolean insertIntoTutors(int id, List<String> subjects) throws SQLException {
        try {
        dslContext.insertInto(TUTORS)
                .set(TUTORS.ID, id)
                .set(TUTORS.SUBJECTS, subjects.toArray(new String[0]))
                .execute();
        // NOTE: Maximum subjects taught by a tutor is 100

            Result<TutorsRecord> resTutors = dslContext.fetch(TUTORS, TUTORS.ID.eq(id));

            // check if insert failed
            return !resTutors.isEmpty();
        } catch (Exception ex) {
            throw new SQLException("Could not insert data into tutors", ex);
        }
    }

    public ResponseEntity<TutorUser> getTutorByEmail(String email) throws SQLException {
        if (StringUtils.isEmpty(email)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            Result<UsersRecord> userData = dslContext.fetch(USERS, USERS.EMAIL.eq(email));
            if (userData.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            UsersRecord usersRecord = userData.get(0);

            Result<TutorsRecord> tutorData = dslContext.fetch(TUTORS, TUTORS.ID.eq(usersRecord.getId()));
            if (tutorData.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(this.buildTutorUser(usersRecord, tutorData.get(0)), HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not query data", ex);
        }
    }

    public ResponseEntity<TutorUser> updateTutor(UpdateTutorRequest updateTutorRequest) throws SQLException {
        if (StringUtils.isEmpty(updateTutorRequest.getEmail())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> resUser = dslContext.fetch(USERS, USERS.EMAIL.eq(updateTutorRequest.getEmail()));

            // user does not exists
            if (resUser.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            UsersRecord user = resUser.get(0);

            // update tutors
            dslContext.update(TUTORS)
                    .set(TUTORS.SUBJECTS, updateTutorRequest.getSubjects().toArray(new String[0]))
                    .where(TUTORS.ID.eq(user.getId()))
                    .execute();

            TutorsRecord resTutor = dslContext.fetch(TUTORS, TUTORS.ID.eq(user.getId())).get(0);

            return new ResponseEntity<>(this.buildTutorUser(user, resTutor), HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not update Tutor", ex);
        }
    }

    public ResponseEntity<List<AvailableHours>> getAvailableHours(String email) throws SQLException {
        if (StringUtils.isEmpty(email)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> resUser = dslContext.fetch(USERS, USERS.EMAIL.eq(email));

            // user does not exists
            if (resUser.isEmpty() || !Boolean.TRUE.equals(resUser.get(0).getTutor())) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            UsersRecord user = resUser.get(0);
            Result<AvailableHoursRecord> availableHoursRecord = dslContext.fetch(AVAILABLE_HOURS, AVAILABLE_HOURS.TUTOR_ID.eq(user.getId()));
            if (availableHoursRecord.isEmpty()) {
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.ACCEPTED);
            }

            List<AvailableHours> availableHoursList = new ArrayList<>();

            for (AvailableHoursRecord availableHours: availableHoursRecord) {
                availableHoursList.add(buildAvailableHours(availableHours));
            }

            return new ResponseEntity<>(availableHoursList, HttpStatus.OK);

        } catch (Exception ex) {
            throw new SQLException("Could not get AvailableHours", ex);
        }
    }

    public ResponseEntity<List<AvailableHours>> modifyAvailableHours(ModifyAvailableHours modifyAvailableHours) throws SQLException {
        if (StringUtils.isEmpty(modifyAvailableHours.getEmail())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> resUser = dslContext.fetch(USERS, USERS.EMAIL.eq(modifyAvailableHours.getEmail()));

            // user does not exists
            if (resUser.isEmpty() || !Boolean.TRUE.equals(resUser.get(0).getTutor())) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // if start time is after end time
            if (modifyAvailableHours.getStartTime().isAfter(modifyAvailableHours.getEndTime())) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            UsersRecord user = resUser.get(0);

            Result<AvailableHoursRecord> availableHoursRecord = dslContext.fetch(AVAILABLE_HOURS, AVAILABLE_HOURS.TUTOR_ID.eq(user.getId()), AVAILABLE_HOURS.DAY_OF_WEEK.eq(modifyAvailableHours.getDayOfWeek().toString()));

            if (availableHoursRecord.isNotEmpty()) {
                // update available hours
                dslContext.update(AVAILABLE_HOURS)
                        .set(AVAILABLE_HOURS.START_TIME, modifyAvailableHours.getStartTime())
                        .set(AVAILABLE_HOURS.END_TIME, modifyAvailableHours.getEndTime())
                        .set(AVAILABLE_HOURS.DAY_OF_WEEK, modifyAvailableHours.getDayOfWeek().toString())
                        .where(AVAILABLE_HOURS.TUTOR_ID.eq(user.getId()))
                        .and(AVAILABLE_HOURS.DAY_OF_WEEK.eq(modifyAvailableHours.getDayOfWeek().toString()))
                        .execute();
            } else {
                // insert into table
                dslContext.insertInto(AVAILABLE_HOURS)
                        .set(AVAILABLE_HOURS.TUTOR_ID, user.getId())
                        .set(AVAILABLE_HOURS.START_TIME, modifyAvailableHours.getStartTime())
                        .set(AVAILABLE_HOURS.END_TIME, modifyAvailableHours.getEndTime())
                        .set(AVAILABLE_HOURS.DAY_OF_WEEK, modifyAvailableHours.getDayOfWeek().toString())
                        .execute();
            }

            return this.getAvailableHours(user.getEmail());
        } catch (Exception ex) {
            throw new SQLException("Could not update AvailableHours", ex);
        }
    }

    public ResponseEntity<HttpStatus> deleteAvailableHours(String email, Days day) throws SQLException {
        if (StringUtils.isEmpty(email)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> resUser = dslContext.fetch(USERS, USERS.EMAIL.eq(email));

            // user does not exists
            if (resUser.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // if start time is after end time
            UsersRecord user = resUser.get(0);

            Result<AvailableHoursRecord> availableHoursRecord = dslContext.fetch(AVAILABLE_HOURS, AVAILABLE_HOURS.TUTOR_ID.eq(user.getId()));

            // if the day path param is present
            if (day != null) {
                availableHoursRecord = dslContext.fetch(AVAILABLE_HOURS, AVAILABLE_HOURS.TUTOR_ID.eq(user.getId()), AVAILABLE_HOURS.DAY_OF_WEEK.eq(day.toString()));
            }

            if (availableHoursRecord.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // delete from table
            if (day == null) {
                dslContext.deleteFrom(AVAILABLE_HOURS)
                        .where(AVAILABLE_HOURS.TUTOR_ID.eq(user.getId()))
                        .execute();
            } else {
                dslContext.deleteFrom(AVAILABLE_HOURS)
                        .where(AVAILABLE_HOURS.TUTOR_ID.eq(user.getId()))
                        .and(AVAILABLE_HOURS.DAY_OF_WEEK.eq(day.toString()))
                        .execute();
            }

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not delete AvailableHours", ex);
        }
    }

    private TutorUser buildTutorUser(UsersRecord usersRecord, TutorsRecord tutorsRecord) {
        TutorUser response = new TutorUser();

        // user data
        response.setId(usersRecord.getId());

        response.setFName(usersRecord.getFName());
        response.setLName(usersRecord.getLName());
        response.setEmail(usersRecord.getEmail());
        // PASSWORD SET AS NULL (SHOULD NOT BE A PART OF THE RESPONSE)
        response.setPassword(null);
        response.setTotalHours(usersRecord.getTotalHours()); // Can we use this as available hours for tutors
        response.setTutor(true);
        response.setProfilePic(usersRecord.getProfilePic());
        response.setAboutMe(usersRecord.getAboutMe());

        // tutor data
        response.setSubjects(Arrays.asList(tutorsRecord.getSubjects()));

        return response;
    }

    private AvailableHours buildAvailableHours(AvailableHoursRecord availableHoursRecord) {
        AvailableHours response = new AvailableHours();

        response.setTutorId(availableHoursRecord.getTutorId());
        response.setStartTime(availableHoursRecord.getStartTime());
        response.setEndTime(availableHoursRecord.getEndTime());
        response.setDayOfWeek(availableHoursRecord.getDayOfWeek());

        return response;
    }
}
