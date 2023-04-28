package onlinetutoring.com.teamelevenbackend.controller.models;

import lombok.Getter;
import lombok.Setter;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments;

@Getter
@Setter
public class AppointmentResponse extends Appointments {
    private String tutorEmail;
    private String studentEmail;
}
