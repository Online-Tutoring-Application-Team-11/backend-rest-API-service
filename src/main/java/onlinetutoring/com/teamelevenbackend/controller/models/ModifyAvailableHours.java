package onlinetutoring.com.teamelevenbackend.controller.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import onlinetutoring.com.teamelevenbackend.models.enums.Days;

import java.time.LocalTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModifyAvailableHours extends AbstractUpdateRequest {
    private LocalTime startTime;
    private LocalTime endTime;
    private Days dayOfWeek;

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public Days getDayOfWeek() {
        return dayOfWeek;
    }
}
