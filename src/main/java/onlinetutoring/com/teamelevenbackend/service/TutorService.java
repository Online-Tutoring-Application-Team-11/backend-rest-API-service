package onlinetutoring.com.teamelevenbackend.service;

import onlinetutoring.com.teamelevenbackend.entity.tables.records.StudentsRecord;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.TutorsRecord;
import org.apache.commons.collections4.CollectionUtils;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static onlinetutoring.com.teamelevenbackend.entity.tables.Tutors.TUTORS;

@Service
public class TutorService {

    @Autowired
    private DSLContext dslContext;

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

    public boolean insertIntoTutors(int id, List<String> subjects) {
        dslContext.insertInto(TUTORS)
                .set(TUTORS.ID, id)
                .set(TUTORS.SUBJECTS, subjects.toArray(new String[100]))
                .execute();
        // NOTE: Maximum subjects taught by a tutor is 100

        Result<TutorsRecord> resTutors = dslContext.fetch(TUTORS, TUTORS.ID.eq(id));

        // check if insert failed
        return !resTutors.isEmpty();
    }
}
