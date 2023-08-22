package net.stelitop.battledudestcg.discord.interactions.autocomplete;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import net.stelitop.battledudestcg.discord.framework.autocomplete.AutocompletionExecutor;
import net.stelitop.battledudestcg.discord.framework.autocomplete.InputSuggestion;
import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CardNameAutocomplete implements AutocompletionExecutor {

    @Autowired
    private CardRepository cardRepository;
    @Override
    public List<InputSuggestion> execute(ChatInputAutoCompleteEvent event) {
        var focusedOptionValue = event.getFocusedOption().getValue();
        String value = focusedOptionValue.isEmpty() ? "" : focusedOptionValue.get().asString().toLowerCase();

        return cardRepository.getAllCardNames().stream()
                .filter(x -> x.toLowerCase().contains(value))
                .map(x -> InputSuggestion.create(x, x))
                .toList();
    }
}
