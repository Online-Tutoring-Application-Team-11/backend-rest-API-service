package onlinetutoring.com.teamelevenbackend.controller.models;

import java.util.List;

public class UpdateTutorRequest extends AbstractUpdateRequest {
    private List<String> subjects;
    public List<String> getSubjects() {
        return subjects;
    }
}
