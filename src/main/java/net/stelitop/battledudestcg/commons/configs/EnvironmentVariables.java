package net.stelitop.battledudestcg.commons.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentVariables {

    private final boolean devmode;

    @Autowired
    private EnvironmentVariables(Environment environment) {
        devmode = Boolean.parseBoolean(environment.getProperty("devmode"));
    }

    public boolean inDevmode() {
        return devmode;
    }
}
