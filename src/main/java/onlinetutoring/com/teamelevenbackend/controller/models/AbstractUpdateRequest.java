package onlinetutoring.com.teamelevenbackend.controller.models;

import java.io.Serial;
import java.io.Serializable;

public abstract class AbstractUpdateRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String email;

    public String getEmail() {
        return email;
    }
}
