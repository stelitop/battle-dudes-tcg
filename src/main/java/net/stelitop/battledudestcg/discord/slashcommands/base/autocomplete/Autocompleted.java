package net.stelitop.battledudestcg.discord.slashcommands.base.autocomplete;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.stelitop.battledudestcg.discord.slashcommands.base.definition.params.OptionalCommandParam;
import net.stelitop.battledudestcg.discord.slashcommands.base.definition.params.CommandParam;
import net.stelitop.battledudestcg.discord.slashcommands.base.requirements.CommandRequirementExecutor;

/**
 * Annotate a {@link net.stelitop.battledudestcg.discord.slashcommands.base.definition.params.CommandParam} or
 * {@link net.stelitop.battledudestcg.discord.slashcommands.base.definition.params.OptionalCommandParam} method
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
