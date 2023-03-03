package onlinetutoring.com.teamelevenbackend.service;

import onlinetutoring.com.teamelevenbackend.api.models.auth.LoginRequest;
import onlinetutoring.com.teamelevenbackend.api.models.auth.UserSignupRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.UsersRecord;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Users;
import org.jasypt.util.password.StrongPasswordEncryptor;
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

    private static final StrongPasswordEncryptor PASSWORD_ENCRYPTOR = new StrongPasswordEncryptor();

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

            // insert into user (profilePic and aboutMe are set as null initially)
            dslContext.insertInto(USERS, USERS.EMAIL,
                            USERS.F_NAME, USERS.L_NAME,
                            USERS.PASSWORD,
                            USERS.ABOUT_ME, USERS.TUTOR, USERS.TOTAL_HOURS, USERS.PROFILE_PIC)
                    .values(userSignupRequest.getEmail(),
                            userSignupRequest.getfName(), userSignupRequest.getlName(),
                            PASSWORD_ENCRYPTOR.encryptPassword(userSignupRequest.getPassword()),
                            null, userSignupRequest.isTutor(), 0, null)
                    .execute();

            Result<UsersRecord> resUser = dslContext.fetch(USERS, USERS.EMAIL.eq(userSignupRequest.getEmail()));

            // check if insert failed
            if (resUser.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            UsersRecord user = resUser.get(0);

            if (userSignupRequest.isTutor()) {
                if (!tutorService.insertIntoTutors(user.getId(), Collections.emptyList())) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } else {
                if (!studentService.insertIntoStudents(user.getId(), Collections.emptyList(), 0)) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }

            return new ResponseEntity<>(this.buildUser(user), HttpStatus.OK);
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
            UsersRecord user = resUser.get(0);

            if (!PASSWORD_ENCRYPTOR.checkPassword(loginRequest.getPassword(), user.getPassword())) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            return new ResponseEntity<>(this.buildUser(user), HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not insert into student table", ex);
        }
    }

    private Users buildUser(UsersRecord usersRecord) {
        Users response = new Users();

        response.setId(usersRecord.getId());
        response.setFName(usersRecord.getFName());
        response.setLName(usersRecord.getLName());
        response.setEmail(usersRecord.getEmail());
        // PASSWORD SET AS NULL (SHOULD NOT BE A PART OF THE RESPONSE)
        response.setPassword(null);
        response.setTotalHours(usersRecord.getTotalHours());
        response.setTutor(usersRecord.getTutor());
        response.setProfilePic(usersRecord.getProfilePic());
        response.setAboutMe(usersRecord.getAboutMe());

        return response;
    }
}