package net.stelitop.battledudestcg.discord.interactions.autocomplete;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.services.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import net.stelitop.mad4j.autocomplete.AutocompletionExecutor;
import net.stelitop.mad4j.autocomplete.InputSuggestion;

import java.util.List;

@Component
public class CardInSelectedDeckAutocomplete implements AutocompletionExecutor {

    @Autowired
    private DeckService deckService;
    @Autowired
    private CardNameFilterer cardNameFilterer;

    @Override
    public List<InputSuggestion> execute(ChatInputAutoCompleteEvent event) {
        long userId = event.getInteraction().getUser().getId().asLong();
        var selectedDeckAction = deckService.getSelectedDeck(userId);
        if (selectedDeckAction.hasFailed()) return List.of();
        var deckEditing = selectedDeckAction.getResponse().getLeft();
        List<Card> cardsInDeck = deckEditing.getEditedDeck().getCards();

        var focusedOptionValue = event.getFocusedOption().getValue();
        String filter = focusedOptionValue.isEmpty() ? "" : focusedOptionValue.get().asString().toLowerCase();

        List<Card> cards = cardNameFilterer.applySpecialFilter(filter, cardsInDeck);

        return cards.stream()
                .map(Card::getName)
                .distinct()
                .map(x -> InputSuggestion.create(x, x))
                .toList();
    }
}
