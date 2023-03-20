package onlinetutoring.com.teamelevenbackend.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupService {
    public boolean IsEmailValid(String email) {

        String regex = "^[a-zA-Z0-9_!#$%&amp;'*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&amp;'*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
