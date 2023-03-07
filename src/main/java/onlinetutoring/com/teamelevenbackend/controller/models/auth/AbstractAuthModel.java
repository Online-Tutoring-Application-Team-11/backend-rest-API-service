package onlinetutoring.com.teamelevenbackend.controller.models.auth;

import java.io.Serial;
import java.io.Serializable;

public abstract class AbstractAuthModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
