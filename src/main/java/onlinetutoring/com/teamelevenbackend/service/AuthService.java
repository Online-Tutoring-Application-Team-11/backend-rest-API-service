package onlinetutoring.com.teamelevenbackend.service;

import onlinetutoring.com.teamelevenbackend.config.JwtService;
import onlinetutoring.com.teamelevenbackend.controller.models.auth.LoginRequest;
import onlinetutoring.com.teamelevenbackend.controller.models.auth.UserSignupRequest;
import onlinetutoring.com.teamelevenbackend.controller.models.auth.UserWithToken;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.UsersRecord;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static onlinetutoring.com.teamelevenbackend.entity.Tables.USERS;

@Component
public class AuthService {

    private static final StrongPasswordEncryptor PASSWORD_ENCRYPTOR = new StrongPasswordEncryptor();

    private DSLContext dslContext;
    private StudentService studentService;
    private TutorService tutorService;
    @Autowired
    public void setInternalAuthService(DSLContext dslContext, TutorService tutorService, StudentService studentService) {
        this.dslContext = dslContext;
        this.tutorService = tutorService;
        this.studentService = studentService;
    }

    private JwtService jwtService;
    @Autowired
    public void setJwtAuth(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public ResponseEntity<UserWithToken> signup(UserSignupRequest userSignupRequest) throws SQLException {
        // Check for empty fields and validated email
        if (StringUtils.isEmpty(userSignupRequest.getEmail())
                || StringUtils.isEmpty(userSignupRequest.getPassword())
                || StringUtils.isEmpty(userSignupRequest.getfName())
                || !isEmailValid(userSignupRequest.getEmail())) {
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

            return new ResponseEntity<>(this.buildUser(user, jwtService.generateToken(userSignupRequest)), HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Signup Failure", ex);
        }
    }

    public ResponseEntity<UserWithToken> login(LoginRequest loginRequest) throws SQLException {
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

            return new ResponseEntity<>(this.buildUser(user, jwtService.generateToken(loginRequest)), HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Login Failure", ex);
        }
    }

    private boolean isEmailValid(String email) {

        String regex = "^[a-zA-Z0-9_!#$%&amp;'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&amp;'*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public UserWithToken buildUser(UsersRecord usersRecord, String token) {
        UserWithToken response = new UserWithToken();

        // user data
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
        response.setToken(token);

        return response;
    }
}


