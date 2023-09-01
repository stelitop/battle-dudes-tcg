package net.stelitop.battledudestcg.discord.interactions.autocomplete;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import net.stelitop.mad4j.autocomplete.AutocompletionExecutor;
import net.stelitop.mad4j.autocomplete.InputSuggestion;
import net.stelitop.battledudestcg.game.database.repositories.ChestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChestNameAutocomplete implements AutocompletionExecutor {

    @Autowired
    private ChestRepository chestRepository;

    public List<InputSuggestion> execute(ChatInputAutoCompleteEvent event) {

        var focusedOptionValue = event.getFocusedOption().getValue();
        String value = focusedOptionValue.isEmpty() ? "" : focusedOptionValue.get().asString().toLowerCase();

        return chestRepository.getAllChestNames().stream()
                .filter(x -> x.toLowerCase().contains(value))
                .map(x -> InputSuggestion.create(x, x))
                .toList();
    }
}
