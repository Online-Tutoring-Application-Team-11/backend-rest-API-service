package onlinetutoring.com.teamelevenbackend.controller.models.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Users;

@RequiredArgsConstructor
public class UserWithToken extends Users {

    @Getter
    @Setter
    private String token;
}
