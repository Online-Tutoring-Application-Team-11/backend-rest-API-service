package onlinetutoring.com.teamelevenbackend.controller;

import onlinetutoring.com.teamelevenbackend.controller.models.UpdateProfileRequest;
import onlinetutoring.com.teamelevenbackend.controller.models.auth.ChangePasswordRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Users;
import onlinetutoring.com.teamelevenbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static onlinetutoring.com.teamelevenbackend.utils.ControllerUtils.BASE_LOCAL;
import static onlinetutoring.com.teamelevenbackend.utils.ControllerUtils.BASE_PRODUCTION;

/**
 * Controller class for handling requests related to user data and account management.
 */
@Controller
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = {BASE_PRODUCTION, BASE_LOCAL}, methods = {RequestMethod.DELETE, RequestMethod.PUT})
@CacheConfig(cacheNames = {"tutors"})
public class UserController {

    private UserService userService;
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Deletes a user account identified by their email address.
     *
     * @param email  The email address of the user to be deleted.
     * @return  ResponseEntity  Contains a success status code.
     * @throws Exception   Couldn't delete the user account.
     */
    @DeleteMapping(value = "/delete/{email}")
    @CacheEvict(allEntries = true)
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable(value = "email", required = false) String email) {
        try {
            return userService.deleteUser(email);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Updates a user's profile information.
     *
     * @param updateProfileRequest   The request object containing the updated user data.
     * @return ResponseEntity   Contains the updated user data.
     * @throws Exception    Couldn't update the user data.
     */
    @PutMapping(value = "/update-profile")
    @CacheEvict(allEntries = true)
    public ResponseEntity<Users> updateProfile(@RequestBody UpdateProfileRequest updateProfileRequest) {
        try {
            return userService.updateProfile(updateProfileRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Changes a user's password.
     *
     * @param changePasswordRequest   The request object containing the user's current and new passwords.
     * @return ResponseEntity   Contains a success status code.
     * @throws Exception    Couldn't reset the password.
     */
    @PutMapping(value = "/change-password")
    public ResponseEntity<HttpStatus> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            return userService.updatePassword(changePasswordRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
