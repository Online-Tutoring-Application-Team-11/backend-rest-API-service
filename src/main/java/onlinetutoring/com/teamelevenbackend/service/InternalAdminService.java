package onlinetutoring.com.teamelevenbackend.service;

import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Internal;
import onlinetutoring.com.teamelevenbackend.entity.tables.records.InternalRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static onlinetutoring.com.teamelevenbackend.entity.Tables.INTERNAL;

@Service
public class InternalAdminService {

    @Autowired
    private DSLContext dslContext;

    public List<Internal> getAllInternalUsers() throws SQLException {
        try {
            List<InternalRecord> records = dslContext.fetch(INTERNAL);
            List<Internal> output = new ArrayList<>();
            for (InternalRecord r: records) {
                output.add(new Internal(r.getId(), r.getName()));
            }

            if (output.isEmpty()) {
                return Collections.emptyList();
            } else {
                return output;
            }
        } catch (Exception ex) {
            throw new SQLException("Could not get internal admins from DB", ex);
        }
    }

    public Internal getInternalUser(String name) {
        InternalRecord data = dslContext.fetch(INTERNAL, INTERNAL.NAME.eq(name)).get(0);
        return new Internal(data.getId(), data.getName());
    }
}
