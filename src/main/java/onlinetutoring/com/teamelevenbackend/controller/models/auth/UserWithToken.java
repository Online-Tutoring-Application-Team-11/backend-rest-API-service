package onlinetutoring.com.teamelevenbackend.controller.models.auth;

import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Users;

public class UserWithToken extends Users {

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
