package net.stelitop.battledudestcg.discord.slashcommands.framework.definition;

import java.lang.annotation.*;

@Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@BaseCommandParam
public @interface CommandParam {
    String name();
    String description();
    CommandParamChoice[] choices() default {};
}
