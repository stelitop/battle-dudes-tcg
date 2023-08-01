package net.stelitop.battledudestcg.commons.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class RandomConfig {

    /**
     * Bean for a randomness generator.
     *
     * @return Randomness object.
     */
    @Bean
    public Random randomness() {
        return new Random();
    }
}
