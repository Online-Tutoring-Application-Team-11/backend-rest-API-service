package onlinetutoring.com.teamelevenbackend.controller.models;

public class UpdateProfileRequest extends AbstractUpdateRequest {
    private String fName;
    private String lName;
    private String profilePic;
    private String aboutMe;
    private String password;

    public String getfName() {
        return fName;
    }

    public String getlName() {
        return lName;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public String getPassword() {
        return password;
    }
}
