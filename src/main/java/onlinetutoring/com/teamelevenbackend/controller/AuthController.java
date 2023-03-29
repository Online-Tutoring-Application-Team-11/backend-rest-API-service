package onlinetutoring.com.teamelevenbackend.controller;

import onlinetutoring.com.teamelevenbackend.controller.models.auth.ChangePasswordRequest;
import onlinetutoring.com.teamelevenbackend.controller.models.auth.LoginRequest;
import onlinetutoring.com.teamelevenbackend.controller.models.auth.UserSignupRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Users;
import onlinetutoring.com.teamelevenbackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static onlinetutoring.com.teamelevenbackend.controller.ControllerUtils.BASE_PRODUCTION;
import static onlinetutoring.com.teamelevenbackend.controller.ControllerUtils.BASE_LOCAL;

@Controller
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {BASE_PRODUCTION, BASE_LOCAL}, methods = {RequestMethod.POST, RequestMethod.PUT})
public class AuthController {

    private AuthService authService;
    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/signup")
    public ResponseEntity<Users> signup(@RequestBody UserSignupRequest userSignupRequest) {
        try {
            return authService.signup(userSignupRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/login")
    public ResponseEntity<Users> login(@RequestBody LoginRequest loginRequest) {
        try {
            return authService.login(loginRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/change-password")
    public ResponseEntity<HttpStatus> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            return authService.updatePassword(changePasswordRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
