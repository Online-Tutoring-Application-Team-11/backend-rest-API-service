package onlinetutoring.com.teamelevenbackend.models;

import lombok.Setter;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Users;

import java.util.List;

public class StudentUser extends Users {

    @Setter
    private List<Integer> favouriteTutorIds;

    @Setter
    private Integer year;
}
