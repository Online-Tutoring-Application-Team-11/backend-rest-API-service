package onlinetutoring.com.teamelevenbackend.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Version {

    private static final String PROJECT_VERSION = "V1";
    private static final String ERROR_INFO = "Invalid Endpoint Hit OR Database Failure OR Deployment Crashed";

    @GetMapping(value = "/api/version")
    public ResponseEntity<String> getVersion() {
        return new ResponseEntity<>(PROJECT_VERSION, HttpStatus.OK);
    }

    @GetMapping(value = "/error")
    public ResponseEntity<String> defaultError() {
        return new ResponseEntity<>(ERROR_INFO, HttpStatus.OK);
    }
}
