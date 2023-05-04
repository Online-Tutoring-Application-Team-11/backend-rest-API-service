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

/**
 * Controller class that handles authentication-related requests.
 * Contains endpoints for user signup and login.
 */
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

    /**
     * Creates a new user account based on the provided user signup request.
     *
     * @param userSignupRequest   The user signup request object containing the information for creating a new user account.
     * @return: ResponseEntity   Contains the created user account with an associated authentication token.
     * @throws Exception   Couldn't create a new user account.
     */
    @PostMapping(value = "/signup")
    public ResponseEntity<UserWithToken> signup(@RequestBody UserSignupRequest userSignupRequest) {
        try {
            return authService.signup(userSignupRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Logs in a user based on the provided login credentials.
     *
     * @param loginRequest  The login request object containing the information for logging in a user.
     * @return: ResponseEntity   Contains the logged user account with an associated authentication token.
     * @throws Exception   The user is not able to log in.
     */
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
