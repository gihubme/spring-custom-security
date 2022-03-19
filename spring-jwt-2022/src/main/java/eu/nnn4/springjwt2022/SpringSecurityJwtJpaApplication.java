package eu.nnn4.springjwt2022;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SpringSecurityJwtJpaApplication {

	public static void main(String[] args) {
		log.info("starting SpringSecurityJwtJpaApplication!!");
		SpringApplication.run(SpringSecurityJwtJpaApplication.class, args);
	}

}
