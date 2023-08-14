package net.stelitop.battledudestcg.discord.slashcommands.implementations.autocomplete;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import net.stelitop.battledudestcg.discord.slashcommands.base.autocomplete.AutocompletionExecutor;
import net.stelitop.battledudestcg.discord.slashcommands.base.autocomplete.InputSuggestion;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.decks.CardDeck;
import net.stelitop.battledudestcg.game.services.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeckNameAutocomplete implements AutocompletionExecutor {

    @Autowired
    private DeckService deckService;

    @Override
    public List<InputSuggestion> execute(ChatInputAutoCompleteEvent event) {
        var focusedOptionValue = event.getFocusedOption().getValue();
        String userInput = focusedOptionValue.isEmpty() ? "" : focusedOptionValue.get().asString().toLowerCase();

        List<CardDeck> decks = deckService.getDecksOfUser(event.getInteraction().getUser().getId().asLong());
        return decks.stream()
                .map(CardDeck::getName)
                .filter(x -> x.toLowerCase().contains(userInput))
                .map(x -> InputSuggestion.create(x, x))
                .toList();
    }
}
