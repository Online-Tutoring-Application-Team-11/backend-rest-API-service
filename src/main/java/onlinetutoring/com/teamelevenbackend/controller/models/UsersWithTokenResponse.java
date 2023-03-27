package onlinetutoring.com.teamelevenbackend.controller.models;

import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Users;

public class UsersWithTokenResponse extends Users {
    String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UsersWithTokenResponse(Users value, String token) {
        super(value);
        this.token = token;
    }
}
