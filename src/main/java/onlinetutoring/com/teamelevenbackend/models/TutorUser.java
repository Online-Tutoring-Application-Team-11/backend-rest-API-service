package onlinetutoring.com.teamelevenbackend.models;
import lombok.Setter;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Users;
import java.util.List;

public class TutorUser extends Users {

    @Setter
    private List<String> subjects;
}
