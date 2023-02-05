package onlinetutoring.com.teamelevenbackend.api;

import onlinetutoring.com.teamelevenbackend.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Version {

    @Autowired
    private BaseService baseService;

    @GetMapping(value = "/version")
    public String getVersion() {
        return baseService.applicationVersion();
    }
}
