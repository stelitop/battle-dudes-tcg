package net.stelitop.battledudestcg.discord.slashcommands.implementations.autocomplete;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import net.stelitop.battledudestcg.discord.slashcommands.base.autocomplete.AutocompletionExecutor;
import net.stelitop.battledudestcg.discord.slashcommands.base.autocomplete.InputSuggestion;
import net.stelitop.battledudestcg.game.database.repositories.ChestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChestNameAutocomplete implements AutocompletionExecutor, ApplicationRunner {

    @Autowired
    private ChestRepository chestRepository;

    private final List<String> chestNames = new ArrayList<>();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        fillPossibleChestNames();
    }

    private void fillPossibleChestNames() {
        chestNames.clear();
        chestNames.addAll(chestRepository.getAllChestNames());
    }

    public List<InputSuggestion> execute(ChatInputAutoCompleteEvent event) {

        var focusedOptionValue = event.getFocusedOption().getValue();
        String value = focusedOptionValue.isEmpty() ? "" : focusedOptionValue.get().asString().toLowerCase();

        return chestNames.stream()
                .filter(x -> x.toLowerCase().contains(value))
                .map(x -> InputSuggestion.create(x, x))
                .toList();
    }
}