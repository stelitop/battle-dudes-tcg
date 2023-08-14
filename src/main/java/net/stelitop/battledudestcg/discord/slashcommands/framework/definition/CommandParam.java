package net.stelitop.battledudestcg.discord.slashcommands.framework.definition;

import java.lang.annotation.*;

@Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandParam {
    String name();
    String description();
    CommandParamChoice[] choices() default {};
    boolean required() default true;
}