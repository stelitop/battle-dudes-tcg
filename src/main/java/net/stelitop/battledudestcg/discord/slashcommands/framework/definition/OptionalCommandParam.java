package net.stelitop.battledudestcg.discord.slashcommands.framework.definition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@BaseCommandParam
public @interface OptionalCommandParam {
    String name();
    String description();
    Class<?> type();
    CommandParamChoice[] choices() default {};
}
