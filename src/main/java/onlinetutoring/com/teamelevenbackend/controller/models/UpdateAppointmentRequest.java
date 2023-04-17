package onlinetutoring.com.teamelevenbackend.controller.models;

import java.time.LocalDateTime;

public class UpdateAppointmentRequest extends AbstractUpdateRequest {
    String subject;
    LocalDateTime requestedStartTime, requestedEndTime;

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


