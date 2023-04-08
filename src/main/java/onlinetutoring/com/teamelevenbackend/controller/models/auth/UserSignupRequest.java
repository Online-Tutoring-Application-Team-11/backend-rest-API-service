package onlinetutoring.com.teamelevenbackend.controller.models.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSignupRequest extends AbstractAuthModel {
    private String fName;
    private String lName;
    private boolean tutor;

    public String getfName() {
        return fName;
    }

    public String getlName() {
        return lName;
    }

    public boolean isTutor() {
        return tutor;
    }
}
