package net.stelitop.battledudestcg.commons.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentVariables {

    private final boolean devmode;
    private final boolean updateCommandsOnStart;

    @Autowired
    private EnvironmentVariables(Environment environment) {
        devmode = Boolean.parseBoolean(environment.getProperty("devmode"));
        updateCommandsOnStart = Boolean.parseBoolean(environment.getProperty("slashcommands.update"));
    }

    public boolean inDevmode() {
        return devmode;
    }
    public boolean updateCommandsOnStart() {
        return updateCommandsOnStart;
    }
}
