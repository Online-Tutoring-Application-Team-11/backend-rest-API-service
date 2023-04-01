package onlinetutoring.com.teamelevenbackend.controller.models.auth;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Users;

@RequiredArgsConstructor
public class UserWithToken extends Users {

    @Setter
    private String token;
}
