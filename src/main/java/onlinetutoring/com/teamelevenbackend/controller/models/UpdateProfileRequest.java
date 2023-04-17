package onlinetutoring.com.teamelevenbackend.controller.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateProfileRequest extends AbstractUpdateRequest {
    private String fName;
    private String lName;
    private String profilePic;
    private String aboutMe;

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
}
