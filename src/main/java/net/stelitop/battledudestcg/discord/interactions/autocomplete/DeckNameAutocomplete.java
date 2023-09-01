package net.stelitop.battledudestcg.discord.interactions.autocomplete;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import net.stelitop.battledudestcg.game.database.entities.collection.CardDeck;
import net.stelitop.battledudestcg.game.services.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import net.stelitop.mad4j.autocomplete.AutocompletionExecutor;
import net.stelitop.mad4j.autocomplete.InputSuggestion;

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
