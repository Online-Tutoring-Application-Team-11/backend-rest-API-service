package onlinetutoring.com.teamelevenbackend.api.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class UpdateTutorRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String fName;
    private String lName;
    private String email;
    private String password;
    private String profilePic;
    private String aboutMe;
    private List<String> subjects;

    public String getfName() {
        return fName;
    }

    public String getlName() {
        return lName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public List<String> getSubjects() {
        return subjects;
    }
}
