package onlinetutoring.com.teamelevenbackend.service;

import onlinetutoring.com.teamelevenbackend.entity.tables.records.UsersRecord;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static onlinetutoring.com.teamelevenbackend.entity.Tables.USERS;

@Component
public class JwtService implements UserDetailsService {

    private DSLContext dslContext;
    @Autowired
    public void setDslContext(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Result<UsersRecord> userData = dslContext.fetch(USERS, USERS.EMAIL.eq(email));
        UsersRecord user = userData.get(0);
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }
}
