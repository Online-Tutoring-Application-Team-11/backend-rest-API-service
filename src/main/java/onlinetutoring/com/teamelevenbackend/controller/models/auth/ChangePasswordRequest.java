package onlinetutoring.com.teamelevenbackend.controller.models.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import onlinetutoring.com.teamelevenbackend.controller.models.AbstractUpdateRequest;

@JsonIgnoreProperties(ignoreUnknown = true)
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
