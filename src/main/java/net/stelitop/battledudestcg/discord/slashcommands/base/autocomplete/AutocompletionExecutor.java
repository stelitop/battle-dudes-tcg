package net.stelitop.battledudestcg.discord.slashcommands.base.autocomplete;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;

import java.util.List;

public interface AutocompletionExecutor {

    /**
     * Executes the event of giving suggestions. The event must NOT be
     * answered in this method, as it is answered later on.
     *
     * @param event The event.
     * @return The suggestions for the event.
     */
    List<InputSuggestion> execute(ChatInputAutoCompleteEvent event);
}
