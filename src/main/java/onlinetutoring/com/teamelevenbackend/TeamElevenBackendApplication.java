package onlinetutoring.com.teamelevenbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TeamElevenBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeamElevenBackendApplication.class, args);
	}

}
