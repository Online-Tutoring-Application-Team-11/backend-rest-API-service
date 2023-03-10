package onlinetutoring.com.teamelevenbackend.controller.models;

import java.util.List;

public class UpdateStudentRequest extends AbstractUpdateRequest {
    private Integer year;
    private List<Integer> favouriteTutorIds;

    public Integer getYear() {
        return year;
    }

    public List<Integer> getFavouriteTutorIds() {
        return favouriteTutorIds;
    }
}
