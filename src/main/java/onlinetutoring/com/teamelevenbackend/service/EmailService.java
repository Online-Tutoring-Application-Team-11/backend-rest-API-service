package onlinetutoring.com.teamelevenbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {

    private static final String COMPANY_EMAIL_ID = "aplusonlinetutoring11@gmail.com";
    private static final String EMAIL_SUBJECT_CONFIRM = "Appointment Confirmation from A+ Tutors";
    private static final String EMAIL_SUBJECT_CANCEL = "Appointment Cancellation from A+ Tutors";
    private static final String EMAIL_SUBJECT_REMAINDER = "Appointment Reminder from A+ Tutors";
    private static final String END_REGARDS = "Regards\n\nA+ Tutors";

    private static final String EMAIL_LINE_1 = "Hello!\n\nYour tutoring appointment " +
                                                "with A+ Tutors for ";
    private static final String WEBSITE = "https://online-tutoring-team-eleven.vercel.app";

    private JavaMailSender mailSender;

    @Autowired
    public void setEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendConfirmationEmail(String toEmail, String fromEmail, String subject, LocalDateTime start) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(COMPANY_EMAIL_ID);
        message.setTo(toEmail);
        message.setSubject(EMAIL_SUBJECT_CONFIRM);

        message.setText( EMAIL_LINE_1 +
                subject +
                " is confirmed with tutor " +
                fromEmail +
                " on " +
                start.getMonth() + "/" + start.getDayOfMonth() + "/" + start.getYear() +
                " at " +
                start.getHour() + ":" + start.getMinute() + "hours." +
                "\n\nPlease visit our website @ " + WEBSITE +
                " in case you want to cancel or reschedule.\n\n" +
                END_REGARDS);

        try {
            mailSender.send(message);
        } catch (Exception ignored) {}
    }

    public void sendCancellationEmail(String toEmail, String fromEmail, String subject, LocalDateTime start) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(COMPANY_EMAIL_ID);
        message.setTo(toEmail);
        message.setSubject(EMAIL_SUBJECT_CANCEL);

        message.setText(EMAIL_LINE_1 +
                subject +
                " with tutor " +
                fromEmail +
                " on " +
                start.getMonth() + "/" + start.getDayOfMonth() + "/" + start.getYear() +
                " at " +
                start.getHour() + ":" + start.getMinute() + " hours " +
                " has been cancelled successfully." +
                "\n\nPlease visit our website @ + " + WEBSITE +" - to schedule a new appointment.\n\n" +
                END_REGARDS);

        try {
            mailSender.send(message);
        } catch (Exception ignored) {}
    }

    public void sendReminderEmail(String toEmail, String fromEmail, String subject, LocalDateTime start) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(COMPANY_EMAIL_ID);
        message.setTo(toEmail);
        message.setSubject(EMAIL_SUBJECT_REMAINDER);

        message.setText("Hello!\n\nThis is a reminder regarding your upcoming" +
                " tutoring appointment with A+ Tutors for " +
                subject +
                " with tutor " +
                fromEmail +
                " on " +
                start.getMonth() + "/" + start.getDayOfMonth() + "/" + start.getYear() +
                " at " +
                start.getHour() + ":" + start.getMinute() + " hours." +
                "\n\nThe appointment will start in 15 minutes, at " +
                start.getHour() + ":" + start.getMinute() + " hours." +
                "\n\nPlease visit our website @ " + WEBSITE + " - to schedule a new appointment.\n\n" +
                END_REGARDS);

        try {
            mailSender.send(message);
        } catch (Exception ignored) {}
    }
}