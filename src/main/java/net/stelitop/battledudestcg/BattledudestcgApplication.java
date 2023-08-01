package net.stelitop.battledudestcg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class BattledudestcgApplication {

	public static void main(String[] args) {
		SpringApplication.run(BattledudestcgApplication.class, args);
	}

}
