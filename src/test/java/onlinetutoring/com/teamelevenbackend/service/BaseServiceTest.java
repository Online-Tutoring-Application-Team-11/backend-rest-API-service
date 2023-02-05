package onlinetutoring.com.teamelevenbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import static onlinetutoring.com.teamelevenbackend.service.BaseService.VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Component
class BaseServiceTest {

    private BaseService baseService;

    @BeforeEach
    public void setup() {
        baseService = new BaseService();
    }

    @Test
    void versionTest() {
        assertEquals(VERSION, baseService.applicationVersion());
    }
}
