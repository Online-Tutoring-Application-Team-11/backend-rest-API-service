package onlinetutoring.com.teamelevenbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final String COMPANY_EMAIL = "anirudh.umarji@gmail.com";
    private static final String EMAIL_SUBJECT = "Appointment Confirmation for A+ Tutors";

    private JavaMailSender mailSender;
    @Autowired
    public void setEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSimpleEmail(String toEmail, String fromEmail, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(COMPANY_EMAIL);
        message.setTo(toEmail);
        message.setSubject(EMAIL_SUBJECT);

        message.setText("Hello!\n\nYour tutoring appointment for "
                + subject
                + " is confirmed with "
                + fromEmail
                + ".\n\nPlease visit our website - https://online-tutoring-team-eleven.vercel.app - if you need to cancel or reschedule.\n\n"
                + "Regards\n\nA+ Tutors");

        mailSender.send(message);
    }
}
