package onlinetutoring.com.teamelevenbackend.service;

import onlinetutoring.com.teamelevenbackend.api.models.UpdateStudentRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.StudentsRecord;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.UsersRecord;
import onlinetutoring.com.teamelevenbackend.models.StudentUser;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static onlinetutoring.com.teamelevenbackend.entity.Tables.STUDENTS;
import static onlinetutoring.com.teamelevenbackend.entity.Tables.USERS;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class StudentService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private TutorService tutorService;

    private static final List<Integer> YEARS = new ArrayList<>(Arrays.asList(0,1,2,3,4));

    public ResponseEntity<StudentUser> getStudentByEmail(String email) throws SQLException {
        if (StringUtils.isEmpty(email)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> userData = dslContext.fetch(USERS, USERS.EMAIL.eq(email));
            if (userData.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            UsersRecord usersRecord = userData.get(0);
            Result<StudentsRecord> studentData = dslContext.fetch(STUDENTS, STUDENTS.ID.eq(usersRecord.getId()));

            StudentUser response = new StudentUser();

            // user data
            response.setId(usersRecord.getId());
            response.setFName(usersRecord.getFName());
            response.setLName(usersRecord.getLName());
            response.setEmail(email);
            response.setPassword(null);
            response.setTotalHours(usersRecord.getTotalHours());
            response.setTutor(false);
            response.setProfilePic(usersRecord.getProfilePic());
            response.setAboutMe(usersRecord.getAboutMe());

            // student data
            response.setFavouriteTutorIds(tutorService.validateIsTutor(Arrays.asList(studentData.get(0).getFavouriteTutorIds())));
            response.setYear(studentData.get(0).getYear());

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception ex) {
            throw new SQLException("Could not query data", ex);
        }
    }

    public boolean insertIntoStudents(int id, List<Integer> favTutorIds, int year) {
        if (this.isInvalidYear(year)) {
            return false;
        }

        // insert into students
        dslContext.insertInto(STUDENTS)
                .set(STUDENTS.ID, id)
                .set(STUDENTS.FAVOURITE_TUTOR_IDS, favTutorIds.toArray(new Integer[100]))
                .set(STUDENTS.YEAR, year)
                .execute();
        // NOTE: Maximum fav tutors for a student is 100

        Result<StudentsRecord> resStudent = dslContext.fetch(STUDENTS, STUDENTS.ID.eq(id));

        // check if insert failed
        return !resStudent.isEmpty();
    }

    public ResponseEntity<HttpStatus> deleteStudent(String email) throws SQLException {
        if (StringUtils.isEmpty(email)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            // check if exists
            Result<UsersRecord> userData = dslContext.fetch(USERS, USERS.EMAIL.eq(email));
            if (userData.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            UsersRecord usersRecord = userData.get(0);
            dslContext.deleteFrom(STUDENTS).where(STUDENTS.ID.eq(usersRecord.getId())).execute();

            dslContext.deleteFrom(USERS).where(USERS.ID.eq(usersRecord.getId())).execute();

            userData = dslContext.fetch(USERS, USERS.EMAIL.eq(email));
            // deletion failed
            if (userData.isNotEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Failed to delete Student", ex);
        }
    }

    public ResponseEntity<StudentUser> updateStudent(UpdateStudentRequest updateStudentRequest) throws SQLException {
        if (StringUtils.isEmpty(updateStudentRequest.getEmail()) || this.isInvalidYear(updateStudentRequest.getYear())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> resUserBefore = dslContext.fetch(USERS, USERS.EMAIL.eq(updateStudentRequest.getEmail()));

            // user does not exists
            if (resUserBefore.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // update user
            dslContext.update(USERS)
                    .set(USERS.F_NAME, updateStudentRequest.getfName())
                    .set(USERS.L_NAME, updateStudentRequest.getlName())
                    .set(USERS.PROFILE_PIC, updateStudentRequest.getProfilePic())
                    .set(USERS.ABOUT_ME, updateStudentRequest.getAboutMe())
                    .set(USERS.PASSWORD, updateStudentRequest.getPassword())
                    .where(USERS.EMAIL.eq(updateStudentRequest.getEmail()))
                    .execute();

            UsersRecord resUser = dslContext.fetch(USERS, USERS.EMAIL.eq(updateStudentRequest.getEmail())).get(0);

            // update students
            dslContext.update(STUDENTS)
                    .set(STUDENTS.FAVOURITE_TUTOR_IDS, updateStudentRequest.getFavouriteTutorIds().toArray(new Integer[100]))
                    .set(STUDENTS.YEAR, updateStudentRequest.getYear())
                    .where(STUDENTS.ID.eq(resUser.getId()))
                    .execute();

            StudentsRecord resStudent = dslContext.fetch(STUDENTS, STUDENTS.ID.eq(resUser.getId())).get(0);

            StudentUser response = this.buildStudentUser(resUser, resStudent);
            if (response == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not update student", ex);
        }
    }

    private StudentUser buildStudentUser(UsersRecord usersRecord, StudentsRecord studentsRecord) {
        if (usersRecord == null || studentsRecord == null) {
            return null;
        }
/*
        A LOT OF CHECKS ARE NOT BEING DONE BECAUSE THE API WAS GETTING SLOW!
        SINCE THIS IS BUILT BY US, WE ARE CONTROLLING THE DATA FLOW
        THESE CHECKS ARE NOT REQUIRED
*/

        // user data
        StudentUser response = new StudentUser();

        response.setId(usersRecord.getId());
        response.setFName(usersRecord.getFName());
        response.setLName(usersRecord.getLName());
        response.setEmail(usersRecord.getEmail());
        response.setPassword(usersRecord.getPassword());
        response.setTotalHours(usersRecord.getTotalHours());
        response.setTutor(usersRecord.getTutor());
        response.setProfilePic(usersRecord.getProfilePic());
        response.setAboutMe(usersRecord.getAboutMe());

        // student data
        response.setFavouriteTutorIds(tutorService.validateIsTutor(Arrays.asList(studentsRecord.getFavouriteTutorIds())));
        response.setYear(studentsRecord.getYear());

        return response;
    }

    private boolean isInvalidYear(Integer year) {
        return !YEARS.contains(year);
    }
}
