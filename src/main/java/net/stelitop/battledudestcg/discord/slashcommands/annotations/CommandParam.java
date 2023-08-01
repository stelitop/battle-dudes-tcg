package net.stelitop.battledudestcg.discord.slashcommands.annotations;

import java.lang.annotation.*;

@Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@BaseCommandParam
public @interface CommandParam {
    String name();
    String description();
}

