package onlinetutoring.com.teamelevenbackend.models;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Users;
import java.util.List;

public class TutorUser extends Users {
    private List<String> subjects;

    public List<String> getSubjects(){ return subjects; }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }
}
