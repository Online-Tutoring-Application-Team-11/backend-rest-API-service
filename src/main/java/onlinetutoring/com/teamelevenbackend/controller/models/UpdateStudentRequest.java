package onlinetutoring.com.teamelevenbackend.controller.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
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
