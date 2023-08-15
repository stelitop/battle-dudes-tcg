package net.stelitop.battledudestcg.discord.slashcommands.implementations.autocomplete;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import net.stelitop.battledudestcg.discord.slashcommands.framework.autocomplete.AutocompletionExecutor;
import net.stelitop.battledudestcg.discord.slashcommands.framework.autocomplete.InputSuggestion;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.entities.collection.ChestOwnership;
import net.stelitop.battledudestcg.game.services.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OwnedChestAutocomplete implements AutocompletionExecutor {

    @Autowired
    private CollectionService collectionService;
    @Override
    public List<InputSuggestion> execute(ChatInputAutoCompleteEvent event) {
        long userId = event.getInteraction().getUser().getId().asLong();
        var collection = collectionService.getUserCollection(userId);
        var eventValue = event.getFocusedOption().getValue();
        String input = eventValue.map(ApplicationCommandInteractionOptionValue::asString).orElse("");
        return collection.getOwnedChests().stream()
                .filter(x -> x.getCount() > 0)
                .map(ChestOwnership::getChest)
                .map(Chest::getName)
                .filter(x -> x.toLowerCase().contains(input.toLowerCase()))
                .map(x -> InputSuggestion.create(x, x))
                .toList();
    }
}
