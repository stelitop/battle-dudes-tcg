package net.stelitop.battledudestcg.discord.slashcommands.framework.definition;

import net.stelitop.battledudestcg.discord.slashcommands.framework.autocomplete.AutocompletionExecutor;
import net.stelitop.battledudestcg.discord.slashcommands.framework.autocomplete.NullAutocompleteExecutor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandParam {
    String name();
    String description();
    CommandParamChoice[] choices() default {};
    boolean required() default true;
    //Autocomplete autocomplete() default @Autocomplete(implementation = NullAutocompleteExecutor.class);
    Class<? extends AutocompletionExecutor> autocomplete() default NullAutocompleteExecutor.class;

}