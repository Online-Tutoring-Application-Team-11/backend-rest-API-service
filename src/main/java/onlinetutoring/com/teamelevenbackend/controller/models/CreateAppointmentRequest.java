package onlinetutoring.com.teamelevenbackend.controller.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAppointmentRequest implements Serializable {
    private String studentEmail;
    private String tutorEmail;
    private String subject;
    private LocalDateTime requestedStartTime;
    private LocalDateTime requestedEndTime;

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getTutorEmail() {
        return tutorEmail;
    }

    public String getSubject() {
        return subject;
    }

    public LocalDateTime getRequestedStartTime() {
        return requestedStartTime;
    }

    public LocalDateTime getRequestedEndTime() {
        return requestedEndTime;
    }
}


