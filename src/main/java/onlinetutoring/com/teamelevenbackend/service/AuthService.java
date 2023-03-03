package onlinetutoring.com.teamelevenbackend.service;

import onlinetutoring.com.teamelevenbackend.api.models.auth.LoginRequest;
import onlinetutoring.com.teamelevenbackend.api.models.auth.UserSignupRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.UsersRecord;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Users;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collections;

import static onlinetutoring.com.teamelevenbackend.entity.Tables.USERS;

@Service
public class AuthService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TutorService tutorService;

    public ResponseEntity<Users> signup(UserSignupRequest userSignupRequest) throws SQLException {
        if (StringUtils.isEmpty(userSignupRequest.getEmail())
                || StringUtils.isEmpty(userSignupRequest.getPassword())
                || StringUtils.isEmpty(userSignupRequest.getfName())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> resUserBefore = dslContext.fetch(USERS, USERS.EMAIL.eq(userSignupRequest.getEmail()));

            // user already exists
            if (resUserBefore.isNotEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // insert into user
            dslContext.insertInto(USERS, USERS.EMAIL, USERS.F_NAME, USERS.L_NAME, USERS.PASSWORD, USERS.ABOUT_ME, USERS.TUTOR, USERS.TOTAL_HOURS, USERS.PROFILE_PIC)
                    .values(userSignupRequest.getEmail(), userSignupRequest.getfName(), userSignupRequest.getlName(), userSignupRequest.getPassword(), userSignupRequest.getAboutMe(), userSignupRequest.isTutor(), 0, userSignupRequest.getProfilePic())
                    .execute();

            Result<UsersRecord> resUser = dslContext.fetch(USERS, USERS.EMAIL.eq(userSignupRequest.getEmail()));

            // check if insert failed
            if (resUser.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            if (userSignupRequest.isTutor()) {
                if (!tutorService.insertIntoTutors(resUser.get(0).getId(), Collections.emptyList())) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } else {
                if (!studentService.insertIntoStudents(resUser.get(0).getId(), Collections.emptyList(), 0)) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }

            Users response = this.buildUser(resUser.get(0));

            if (response == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not insert into student table", ex);
        }
    }

    public ResponseEntity<Users> login(LoginRequest loginRequest) throws SQLException {
        if (StringUtils.isEmpty(loginRequest.getEmail())
                || StringUtils.isEmpty(loginRequest.getPassword())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> resUser = dslContext.fetch(USERS, USERS.EMAIL.eq(loginRequest.getEmail()));

            // user does not exist
            if (resUser.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            if (!loginRequest.getPassword().equals(resUser.get(0).getPassword())) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Users response = this.buildUser(resUser.get(0));

            if (response == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not insert into student table", ex);
        }
    }

    private Users buildUser(UsersRecord usersRecord) {
        if (usersRecord == null) {
            return null;
        }

        Users response = new Users();

        response.setId(usersRecord.getId());
        response.setFName(usersRecord.getFName());
        response.setLName(usersRecord.getLName());
        response.setEmail(usersRecord.getEmail());
        response.setPassword(usersRecord.getPassword());
        response.setTotalHours(usersRecord.getTotalHours());
        response.setTutor(usersRecord.getTutor());
        response.setProfilePic(usersRecord.getProfilePic());
        response.setAboutMe(usersRecord.getAboutMe());

        return response;
    }
}
