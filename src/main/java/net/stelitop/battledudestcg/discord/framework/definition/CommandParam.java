package net.stelitop.battledudestcg.discord.framework.definition;

import net.stelitop.battledudestcg.discord.framework.autocomplete.AutocompletionExecutor;
import net.stelitop.battledudestcg.discord.framework.autocomplete.NullAutocompleteExecutor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A single parameter of a slash command.
 */
@Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandParam {
    /**
     * The name of the parameter that will be shown to the user.
     */
    String name();

    /**
     * The description of the parameter that will be shown to the user.
     */
    String description();

    /**
     * The specific choices that the user can choose from for this parameter.
     * Optional setting.
     */
    CommandParamChoice[] choices() default {};
    /**
     * Whether the user must fill out this option. If set to false, the user
     * can decide to leave this option empty. In this case, null will be injected
     * into the field, unless there is a default value given.
     */
    boolean required() default true;

    /**
     * The class that gives autofill suggestions for this method.
     */
    Class<? extends AutocompletionExecutor> autocomplete() default NullAutocompleteExecutor.class;

}