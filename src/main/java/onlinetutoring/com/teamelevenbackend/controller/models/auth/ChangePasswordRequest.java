package onlinetutoring.com.teamelevenbackend.controller.models.auth;

import onlinetutoring.com.teamelevenbackend.controller.models.AbstractUpdateRequest;

public class ChangePasswordRequest extends AbstractUpdateRequest {
    private String password;

    private String newPassword;

    public String getPassword() {
        return password;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
