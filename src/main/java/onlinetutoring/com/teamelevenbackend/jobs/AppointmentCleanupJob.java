package onlinetutoring.com.teamelevenbackend.jobs;

import onlinetutoring.com.teamelevenbackend.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class AppointmentCleanupJob {

    private AppointmentService appointmentService;
    @Autowired
    public void setAppointmentCleanupJob(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Scheduled(fixedDelayString = "PT6H")
    private void cleanupAppointments() {
        appointmentService.cleanup();
    }
}
