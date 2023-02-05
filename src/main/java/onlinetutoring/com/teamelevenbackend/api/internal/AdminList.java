package onlinetutoring.com.teamelevenbackend.api.internal;

import onlinetutoring.com.teamelevenbackend.entity.tables.pojos.Internal;
import onlinetutoring.com.teamelevenbackend.service.InternalAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@Controller
@RestController
@RequestMapping("/api/internal")
public class AdminList {

    @Autowired
    private InternalAdminService internalAdminService;

    @GetMapping(value = "/members")
    public List<Internal> getMemberList() throws SQLException {
        return internalAdminService.getAllInternalUsers();
    }

    @GetMapping(value = "/member/{name}")
    public Internal getMember(@PathVariable("name") String name) {
        return internalAdminService.getInternalUser(name);
    }
}
