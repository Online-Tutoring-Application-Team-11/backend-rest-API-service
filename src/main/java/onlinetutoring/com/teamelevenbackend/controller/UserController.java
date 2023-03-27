package onlinetutoring.com.teamelevenbackend.controller;

import onlinetutoring.com.teamelevenbackend.controller.models.UpdateProfileRequest;
import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Users;
import onlinetutoring.com.teamelevenbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @DeleteMapping(value = "/delete/{email}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable("email") String email) {
        try {
            return userService.deleteUser(email);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/update-profile")
    public ResponseEntity<Users> updateProfile(@RequestBody UpdateProfileRequest updateProfileRequest) {
        try {
            return userService.updateProfile(updateProfileRequest);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
