package onlinetutoring.com.teamelevenbackend.api.models;

import java.util.List;

public class UpdateStudentRequest extends CreateStudentRequest {
    private List<Integer> favouriteTutorIds;

    public List<Integer> getFavouriteTutorIds() {
        return favouriteTutorIds;
    }
}
