package net.stelitop.battledudestcg.discord.slashcommands.framework.autocomplete;

import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.CommandParam;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a {@link CommandParam} method
 * in a slash command to mark it for autocompletion.
 */
@Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Autocompleted {

    /**
     * The class that implements the autocomplete event.
     *
     * @return The class.
     */
    Class<? extends AutocompletionExecutor> implementation();
}
