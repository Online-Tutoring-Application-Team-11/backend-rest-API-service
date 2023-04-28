package onlinetutoring.com.teamelevenbackend.jobs;

import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Appointments;
import onlinetutoring.com.teamelevenbackend.service.AppointmentService;
import onlinetutoring.com.teamelevenbackend.service.EmailService;
import onlinetutoring.com.teamelevenbackend.service.UserService;
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
    private UserService userService;
    @Autowired
    public void setEmailReminderJob(EmailService emailService, AppointmentService appointmentService, UserService userService) {
        this.emailService = emailService;
        this.appointmentService = appointmentService;
        this.userService = userService;
    }

    @Scheduled(fixedDelayString = "PT1M")
    private void sendEmailReminders() {
        List<Appointments> emailList = appointmentService.getReminderAppointments();

        for (Appointments app : emailList) {
            emailService.sendReminderEmail(userService.getEmailById(app.getStudentId()),
                    userService.getEmailById(app.getTutorId()), app.getSubject(), app.getStartTime());

            emailService.sendReminderEmail(userService.getEmailById(app.getTutorId()),
                    userService.getEmailById(app.getStudentId()), app.getSubject(), app.getStartTime());
        }
    }
}
