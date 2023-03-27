package onlinetutoring.com.teamelevenbackend.service;

import onlinetutoring.com.teamelevenbackend.controller.models.UsersWithTokenResponse;
import onlinetutoring.com.teamelevenbackend.controller.models.auth.ChangePasswordRequest;
import onlinetutoring.com.teamelevenbackend.controller.models.auth.LoginRequest;
import onlinetutoring.com.teamelevenbackend.controller.models.auth.UserSignupRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.UsersRecord;
import onlinetutoring.com.teamelevenbackend.util.JwtUtil;
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
    @Autowired
    public void setDslContext(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    private StudentService studentService;
    @Autowired
    public void setStudentService(StudentService studentService) {
        this.studentService = studentService;
    }

    private TutorService tutorService;
    @Autowired
    public void setTutorService(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    private JwtUtil jwtUtil;
    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    private UserService userService;
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public ResponseEntity<UsersWithTokenResponse> signup(UserSignupRequest userSignupRequest) throws SQLException {
        // Check for empty fields and validated email
        if (StringUtils.isEmpty(userSignupRequest.getEmail())
                || StringUtils.isEmpty(userSignupRequest.getPassword())
                || StringUtils.isEmpty(userSignupRequest.getfName())
                || !IsEmailValid(userSignupRequest.getEmail())) {
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

            return new ResponseEntity<>(new UsersWithTokenResponse(userService.buildUser(user), jwtUtil.generateToken(userSignupRequest.getEmail())), HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Signup Failure", ex);
        }
    }

    public ResponseEntity<UsersWithTokenResponse> login(LoginRequest loginRequest) throws SQLException {
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

            return new ResponseEntity<>(new UsersWithTokenResponse(userService.buildUser(user), jwtUtil.generateToken(loginRequest.getEmail())), HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Login Failure", ex);
        }
    }

    public ResponseEntity<HttpStatus> updatePassword(ChangePasswordRequest changePasswordRequest) throws SQLException {
        if (StringUtils.isEmpty(changePasswordRequest.getEmail())
                || StringUtils.isEmpty(changePasswordRequest.getPassword())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> resUser = dslContext.fetch(USERS, USERS.EMAIL.eq(changePasswordRequest.getEmail()));

            // user does not exist
            if (resUser.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            UsersRecord user = resUser.get(0);

            if (Boolean.FALSE.equals(PASSWORD_ENCRYPTOR.checkPassword(changePasswordRequest.getPassword(), user.getPassword()))) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            dslContext.update(USERS)
                    .set(USERS.PASSWORD, PASSWORD_ENCRYPTOR.encryptPassword(changePasswordRequest.getNewPassword()))
                    .where(USERS.EMAIL.eq(changePasswordRequest.getEmail()))
                    .execute();

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Update password failed", ex);
        }
    }


    private boolean IsEmailValid(String email) {

        String regex = "^[a-zA-Z0-9_!#$%&amp;'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&amp;'*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}


