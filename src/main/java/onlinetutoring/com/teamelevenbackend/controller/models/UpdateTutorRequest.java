package onlinetutoring.com.teamelevenbackend.controller.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateTutorRequest extends AbstractUpdateRequest {
    private List<String> subjects;

    public List<String> getSubjects() {
        return subjects;
    }
}
