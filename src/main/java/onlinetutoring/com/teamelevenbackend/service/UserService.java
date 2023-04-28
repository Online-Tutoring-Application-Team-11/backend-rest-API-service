package onlinetutoring.com.teamelevenbackend.service;

import onlinetutoring.com.teamelevenbackend.controller.models.UpdateProfileRequest;
import onlinetutoring.com.teamelevenbackend.controller.models.auth.AbstractAuthModel;
import onlinetutoring.com.teamelevenbackend.controller.models.auth.ChangePasswordRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Users;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.AppointmentsRecord;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.UsersRecord;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static onlinetutoring.com.teamelevenbackend.entity.Tables.APPOINTMENTS;
import static onlinetutoring.com.teamelevenbackend.entity.Tables.USERS;
import static onlinetutoring.com.teamelevenbackend.entity.Tables.TUTORS;
import static onlinetutoring.com.teamelevenbackend.entity.Tables.STUDENTS;

@Controller
public class UserService {

    private static final StrongPasswordEncryptor PASSWORD_ENCRYPTOR = new StrongPasswordEncryptor();

    private DSLContext dslContext;
    @Autowired
    public void setDslContext(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public UsersRecord get(String email) {
        Result<UsersRecord> userData = dslContext.fetch(onlinetutoring.com.teamelevenbackend.entity.tables.Users.USERS, onlinetutoring.com.teamelevenbackend.entity.tables.Users.USERS.EMAIL.eq(email));
        if (userData.isEmpty()) {
            return null;
        }
        return userData.get(0);
    }

    public String getEmailById(int id) {
        try {
            Result<UsersRecord> resUser = dslContext.fetch(USERS, USERS.ID.eq(id));
            return resUser.get(0).getEmail();
        } catch (Exception ex) {
            return null;
        }
    }

    public Optional<AbstractAuthModel> findByEmail(String email) {
        Result<UsersRecord> result = dslContext.fetch(USERS, USERS.EMAIL.eq(email));
        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new AbstractAuthModel(result.get(0).getEmail(), result.get(0).getPassword()));
    }

    public ResponseEntity<HttpStatus> deleteUser(String email) throws SQLException {
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

            if (Boolean.TRUE.equals(usersRecord.getTutor())) {
                dslContext.deleteFrom(TUTORS).where(TUTORS.ID.eq(usersRecord.getId())).execute();
            } else {
                dslContext.deleteFrom(STUDENTS).where(STUDENTS.ID.eq(usersRecord.getId())).execute();
            }

            dslContext.deleteFrom(USERS).where(USERS.ID.eq(usersRecord.getId())).execute();

            userData = dslContext.fetch(USERS, USERS.EMAIL.eq(email));

            if (userData.isNotEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Failed to delete User", ex);
        }
    }

    public ResponseEntity<Users> updateProfile(UpdateProfileRequest updateProfileRequest) throws SQLException {
        if (StringUtils.isEmpty(updateProfileRequest.getEmail())
                || StringUtils.isEmpty(updateProfileRequest.getfName())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Result<UsersRecord> resUser = dslContext.fetch(USERS, USERS.EMAIL.eq(updateProfileRequest.getEmail()));

            // user does not exists
            if (resUser.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // update user
            dslContext.update(USERS)
                    .set(USERS.F_NAME, updateProfileRequest.getfName())
                    .set(USERS.L_NAME, updateProfileRequest.getlName())
                    .set(USERS.PROFILE_PIC, updateProfileRequest.getProfilePic())
                    .set(USERS.ABOUT_ME, updateProfileRequest.getAboutMe())
                    .where(USERS.EMAIL.eq(updateProfileRequest.getEmail()))
                    .execute();

            UsersRecord user = dslContext.fetch(USERS, USERS.EMAIL.eq(updateProfileRequest.getEmail())).get(0);

            return new ResponseEntity<>(buildUser(user), HttpStatus.OK);
        } catch (Exception ex) {
            throw new SQLException("Could not update user", ex);
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

    public void updateTotalHours(UsersRecord user) {
        if (user == null) {
            return;
        }

        Result<AppointmentsRecord> appointmentsRecord;

        if (Boolean.TRUE.equals(user.getTutor())) {
             appointmentsRecord = dslContext.fetch(APPOINTMENTS,
                    APPOINTMENTS.TUTOR_ID.eq(user.getId()),
                    APPOINTMENTS.END_TIME.le(LocalDateTime.now()));
        } else {
            appointmentsRecord = dslContext.fetch(APPOINTMENTS,
                    APPOINTMENTS.STUDENT_ID.eq(user.getId()),
                    APPOINTMENTS.END_TIME.le(LocalDateTime.now()));
        }

        if (appointmentsRecord.isEmpty()) {
            return;
        }

        int totalDuration = 0;
        for (AppointmentsRecord app : appointmentsRecord) {
            totalDuration += Math.toIntExact(Duration.between(app.getStartTime(), app.getEndTime()).toHours());
        }

        dslContext.update(USERS)
                .set(USERS.TOTAL_HOURS, totalDuration)
                .execute();
    }

    private static Users buildUser(UsersRecord usersRecord) {
        Users response = new Users();

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

        return response;
    }
}
