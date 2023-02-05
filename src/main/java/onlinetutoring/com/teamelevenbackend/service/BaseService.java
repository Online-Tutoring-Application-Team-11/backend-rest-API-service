package onlinetutoring.com.teamelevenbackend.service;

import org.springframework.stereotype.Service;

@Service
public class BaseService {

    public static final String VERSION = "v1";

    public String applicationVersion() {
        return VERSION;
    }
}
