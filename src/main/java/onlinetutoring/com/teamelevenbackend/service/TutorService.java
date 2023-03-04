package onlinetutoring.com.teamelevenbackend.service;

import onlinetutoring.com.teamelevenbackend.entity.tables.records.TutorsRecord;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.UsersRecord;
import onlinetutoring.com.teamelevenbackend.models.TutorUser;
import org.apache.commons.collections4.CollectionUtils;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static onlinetutoring.com.teamelevenbackend.entity.Tables.USERS;
import static onlinetutoring.com.teamelevenbackend.entity.Tables.TUTORS;

@Component
public class TutorService {

    private DSLContext dslContext;
    @Autowired
    public void setDslContext(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public ResponseEntity<List<TutorUser>> getAllTutors() throws SQLException {
        try {
            // fetching all tutors
            Result<TutorsRecord> allTutors = dslContext.fetch(TUTORS, TUTORS.ID.ge(0));

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

    private TutorUser buildTutorUser(UsersRecord usersRecord, TutorsRecord tutorsRecord) {
        TutorUser response = new TutorUser();

        // user data
        response.setId(usersRecord.getId());
        response.setFName(usersRecord.getFName());
        response.setLName(usersRecord.getLName());
        response.setEmail(usersRecord.getEmail());
        // PASSWORD SET AS NULL (SHOULD NOT BE A PART OF THE RESPONSE)
        response.setPassword(null);
        response.setTotalHours(usersRecord.getTotalHours());
        response.setTutor(true);
        response.setProfilePic(usersRecord.getProfilePic());
        response.setAboutMe(usersRecord.getAboutMe());

        // tutor data
        response.setSubjects(Arrays.asList(tutorsRecord.getSubjects()));

        return response;
    }
}
