package onlinetutoring.com.teamelevenbackend.jobs;

import onlinetutoring.com.teamelevenbackend.controller.models.AppointmentResponse;
import onlinetutoring.com.teamelevenbackend.service.AppointmentService;
import onlinetutoring.com.teamelevenbackend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class EmailReminderJob {

    private EmailService emailService;
    private AppointmentService appointmentService;
    @Autowired
    public void setEmailReminderJob(EmailService emailService, AppointmentService appointmentService) {
        this.emailService = emailService;
        this.appointmentService = appointmentService;
    }

    @Scheduled(fixedDelayString = "PT1M")
    private void sendEmailReminders() {
        List<AppointmentResponse> emailList = appointmentService.getReminderAppointments();

        for (AppointmentResponse app : emailList) {
            emailService.sendReminderEmail(app.getStudentEmail(),
                    app.getTutorEmail(), app.getSubject(), app.getStartTime());

            emailService.sendReminderEmail(app.getTutorEmail(),
                    app.getStudentEmail(), app.getSubject(), app.getStartTime());
        }
    }
}
