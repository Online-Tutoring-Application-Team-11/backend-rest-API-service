package onlinetutoring.com.teamelevenbackend.api.models.auth;

public class UserSignupRequest extends AbstractAuthModel {
    private String fName;
    private String lName;
    private boolean tutor;
    private String profilePic;
    private String aboutMe;

    public String getfName() {
        return fName;
    }

    public String getlName() {
        return lName;
    }

    public boolean isTutor() {
        return tutor;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public String getAboutMe() {
        return aboutMe;
    }
}
