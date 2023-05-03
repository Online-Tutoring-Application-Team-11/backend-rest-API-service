package onlinetutoring.com.teamelevenbackend.service;

import onlinetutoring.com.teamelevenbackend.controller.models.UpdateStudentRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.StudentsRecord;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.UsersRecord;
import onlinetutoring.com.teamelevenbackend.models.StudentUser;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import static onlinetutoring.com.teamelevenbackend.entity.Tables.STUDENTS;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class StudentService {

    private static final List<Integer> YEARS = new ArrayList<>(Arrays.asList(0,1,2,3,4));

    private UserService userService;
    private DSLContext dslContext;
    private TutorService tutorService;
    @Autowired
    public void setInternalAuthService(UserService userService, DSLContext dslContext, TutorService tutorService) {
        this.userService = userService;
        this.dslContext = dslContext;
        this.tutorService = tutorService;
    }

    /**
     * Get the student using the email
     *
     * @param email The email of the student
     * @return The student
     * @throws SQLException Couldn't query the data
     */
    public ResponseEntity<StudentUser> getStudentByEmail(String email) throws SQLException {
        if (StringUtils.isEmpty(email)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            UsersRecord usersRecord = userService.get(email);

            Result<StudentsRecord> studentData = dslContext.fetch(STUDENTS, STUDENTS.ID.eq(usersRecord.getId()));
            if (studentData.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(this.buildStudentUser(usersRecord, studentData.get(0)), HttpStatus.OK);

        } catch (Exception ex) {
            throw new SQLException("Could not query data", ex);
        }
    }


    /**
     * The insertion into the students using the data
     *
     * @param id The id of the student
     * @param favTutorIds The tutor ids of favorite tutors
     * @param year The year of the student
     * @return The result of the insertion
     * @throws SQLException The student couldn't be inserted
     */
    public boolean insertIntoStudents(int id, List<Integer> favTutorIds, int year) throws SQLException {
        try {
            if (this.isInvalidYear(year)) {
                return false;
            }

            dslContext.insertInto(STUDENTS)
                    .set(STUDENTS.ID, id)
                    .set(STUDENTS.FAVOURITE_TUTOR_IDS, favTutorIds.toArray(new Integer[0]))
                    .set(STUDENTS.YEAR, year)
                    .execute();
            // NOTE: Maximum fav tutors for a student is 100

            Result<StudentsRecord> resStudent = dslContext.fetch(STUDENTS, STUDENTS.ID.eq(id));

            // check if insert failed
            return !resStudent.isEmpty();
        } catch (Exception ex) {
            throw new SQLException("Could not insert data into students", ex);
        }
    }

    /**
     * Updating the student using the UpdateStudentRequest
     *
     * @param updateStudentRequest The updateStudentRequest for the student
     * @return The student
     * @throws SQLException The student couldn't be updated
     */
    public ResponseEntity<StudentUser> updateStudent(UpdateStudentRequest updateStudentRequest) throws SQLException {
        if (StringUtils.isEmpty(updateStudentRequest.getEmail()) || this.isInvalidYear(updateStudentRequest.getYear())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            UsersRecord user = userService.get(updateStudentRequest.getEmail());

            // update students
            dslContext.update(STUDENTS)
                    .set(STUDENTS.FAVOURITE_TUTOR_IDS, updateStudentRequest.getFavouriteTutorIds().toArray(new Integer[0]))
                    .set(STUDENTS.YEAR, updateStudentRequest.getYear())
                    .where(STUDENTS.ID.eq(user.getId()))
                    .execute();

            StudentsRecord resStudent = dslContext.fetch(STUDENTS, STUDENTS.ID.eq(user.getId())).get(0);

            return new ResponseEntity<>(this.buildStudentUser(user, resStudent), HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not update student", ex);
        }
    }

    /**
     * Build student user using the data
     *
     * @param usersRecord The user record for the student
     * @param studentsRecord The student record for the student
     * @return The student
     */
    private StudentUser buildStudentUser(UsersRecord usersRecord, StudentsRecord studentsRecord) {
        StudentUser response = new StudentUser();

        // user data
        response.setId(usersRecord.getId());
        response.setFName(usersRecord.getFName());
        response.setLName(usersRecord.getLName());
        response.setEmail(usersRecord.getEmail());
        // PASSWORD SET AS NULL (SHOULD NOT BE A PART OF THE RESPONSE)
        response.setPassword(null);
        response.setTotalHours(usersRecord.getTotalHours());
        response.setTutor(false);
        response.setProfilePic(usersRecord.getProfilePic());
        response.setAboutMe(usersRecord.getAboutMe());

        // student data
        response.setFavouriteTutorIds(tutorService.validateIsTutor(Arrays.asList(studentsRecord.getFavouriteTutorIds())));
        response.setYear(studentsRecord.getYear());

        return response;
    }

    /**
     * Checking if the year is invalid
     *
     * @param year The entered year
     * @return Result stating if the year is invalid
     */
    private boolean isInvalidYear(Integer year) {
        return !YEARS.contains(year);
    }
}
