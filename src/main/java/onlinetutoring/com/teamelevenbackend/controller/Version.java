package onlinetutoring.com.teamelevenbackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for handling the project version.
 */
@RestController
@CrossOrigin
public class Version {

    private static final String PROJECT_VERSION = "V1";
    private static final String ERROR_INFO = "Invalid Endpoint Hit OR Database Failure OR Deployment Crashed";

    /**
     * Retrieves the current project version.
     *
     * @return ResponseEntity  Contains the project version and a HttpStatus code.
     */
    @GetMapping(value = "/version")
    public ResponseEntity<String> getVersion() {
        return new ResponseEntity<>(PROJECT_VERSION, HttpStatus.OK);
    }

    /**
     * Returns error message.
     *
     * @return ResponseEntity   Contains the error message and a HttpStatus code.
     */
    @GetMapping(value = "/error")
    public ResponseEntity<String> defaultError() {
        return new ResponseEntity<>(ERROR_INFO, HttpStatus.OK);
    }
}
