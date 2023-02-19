package onlinetutoring.com.teamelevenbackend.service;

import onlinetutoring.com.teamelevenbackend.api.models.CreateStudentRequest;
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
import java.util.Arrays;

@Service
public class StudentService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private TutorService tutorService;

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

    public ResponseEntity<StudentUser> createStudent(CreateStudentRequest createStudentRequest) throws SQLException {
        if (StringUtils.isEmpty(createStudentRequest.getEmail())
                || StringUtils.isEmpty(createStudentRequest.getPassword())
                || StringUtils.isEmpty(createStudentRequest.getfName())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> resUserBefore = dslContext.fetch(USERS, USERS.EMAIL.eq(createStudentRequest.getEmail()));

            // user already exists
            if (resUserBefore.isNotEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // insert into user
            dslContext.insertInto(USERS, USERS.EMAIL, USERS.F_NAME, USERS.L_NAME, USERS.PASSWORD, USERS.ABOUT_ME, USERS.TUTOR, USERS.TOTAL_HOURS, USERS.PROFILE_PIC)
                    .values(createStudentRequest.getEmail(), createStudentRequest.getfName(), createStudentRequest.getlName(), createStudentRequest.getPassword(), createStudentRequest.getAboutMe(), false, 0, createStudentRequest.getProfilePic())
                    .execute();

//                    .set(USERS.EMAIL, createStudentRequest.getEmail())
//                    .set(USERS.F_NAME, createStudentRequest.getfName())
//                    .set(USERS.L_NAME, createStudentRequest.getlName())
//                    .set(USERS.PASSWORD, createStudentRequest.getPassword())
//                    .set(USERS.TUTOR, Boolean.FALSE)
//                    .set(USERS.ABOUT_ME, createStudentRequest.getAboutMe())
//                    .set(USERS.PROFILE_PIC, createStudentRequest.getProfilePic())
//                    .set(USERS.TOTAL_HOURS, 0)
//                    .execute();

            Result<UsersRecord> resUser = dslContext.fetch(USERS, USERS.EMAIL.eq(createStudentRequest.getEmail()));

            // check if insert failed
            if (resUser.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            UsersRecord ru = resUser.get(0);

            // insert into students
            dslContext.insertInto(STUDENTS)
                    .set(STUDENTS.ID, ru.getId())
                    .set(STUDENTS.FAVOURITE_TUTOR_IDS, new Integer[0])
                    .set(STUDENTS.YEAR, createStudentRequest.getYear())
                    .execute();

            Result<StudentsRecord> resStudent = dslContext.fetch(STUDENTS, STUDENTS.ID.eq(ru.getId()));

            // check if insert failed
            if (resStudent.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            StudentUser response = this.buildStudentUser(ru, resStudent.get(0));
            if (response == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not insert into table", ex);
        }
    }

    private StudentUser buildStudentUser(UsersRecord usersRecord, StudentsRecord studentsRecord) {
        if (usersRecord == null || studentsRecord == null) {
            return null;
        }

        if (usersRecord.getId() == null || !usersRecord.getId().equals(studentsRecord.getId())) {
            return null;
        }

        if (usersRecord.getFName() == null || usersRecord.getPassword() == null) {
            return null;
        }

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
}
