package onlinetutoring.com.teamelevenbackend.models;
import lombok.Getter;
import lombok.Setter;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.AvailableHours;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Users;
import java.util.List;

@Getter
@Setter
public class TutorUser extends Users {
    private List<String> subjects;
    private List<AvailableHours> availableHours;
}
