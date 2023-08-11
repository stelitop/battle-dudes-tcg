package net.stelitop.battledudestcg;

import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class BattledudestcgApplication {

	public static void main(String[] args) {
		OpenCV.loadShared();
		SpringApplication.run(BattledudestcgApplication.class, args);
	}
}
