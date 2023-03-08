package onlinetutoring.com.teamelevenbackend.service;

import onlinetutoring.com.teamelevenbackend.entity.tables.records.UsersRecord;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.sql.SQLException;

import static onlinetutoring.com.teamelevenbackend.entity.Tables.*;
import static onlinetutoring.com.teamelevenbackend.entity.Tables.STUDENTS;

@Controller
public class UserService {
    private DSLContext dslContext;
    @Autowired
    public void setDslContext(DSLContext dslContext) {
        this.dslContext = dslContext;
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

            if (usersRecord.getTutor()) {
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
}
