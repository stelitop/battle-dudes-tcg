package net.stelitop.battledudestcg.discord.slashcommands.base.definition.params;

import net.stelitop.battledudestcg.discord.slashcommands.base.definition.CommandParamChoice;

import java.lang.annotation.*;

@Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@BaseCommandParam
public @interface CommandParam {
    String name();
    String description();
    CommandParamChoice[] choices() default {};
}

