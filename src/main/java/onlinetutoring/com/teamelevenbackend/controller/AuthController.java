package onlinetutoring.com.teamelevenbackend.controller;

import onlinetutoring.com.teamelevenbackend.controller.models.auth.LoginRequest;
import onlinetutoring.com.teamelevenbackend.controller.models.auth.UserSignupRequest;
import onlinetutoring.com.teamelevenbackend.controller.models.auth.UserWithToken;
import onlinetutoring.com.teamelevenbackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static onlinetutoring.com.teamelevenbackend.utils.ControllerUtils.BASE_PRODUCTION;
import static onlinetutoring.com.teamelevenbackend.utils.ControllerUtils.BASE_LOCAL;

@Controller
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {BASE_PRODUCTION, BASE_LOCAL}, methods = {RequestMethod.POST})
@CacheConfig(cacheNames = {"tutors"})
public class AuthController {

    private AuthService authService;
    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/signup")
    public ResponseEntity<UserWithToken> signup(@RequestBody UserSignupRequest userSignupRequest) {
        try {
            return authService.signup(userSignupRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/login")
    @CacheEvict(allEntries = true)
    public ResponseEntity<UserWithToken> login(@RequestBody LoginRequest loginRequest) {
        try {
            return authService.login(loginRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
