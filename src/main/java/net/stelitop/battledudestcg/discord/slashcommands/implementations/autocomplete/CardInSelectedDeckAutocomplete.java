package net.stelitop.battledudestcg.discord.slashcommands.implementations.autocomplete;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import net.stelitop.battledudestcg.discord.framework.autocomplete.AutocompletionExecutor;
import net.stelitop.battledudestcg.discord.framework.autocomplete.InputSuggestion;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.services.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CardInSelectedDeckAutocomplete implements AutocompletionExecutor {

    @Autowired
    private DeckService deckService;

    @Override
    public List<InputSuggestion> execute(ChatInputAutoCompleteEvent event) {
        long userId = event.getInteraction().getUser().getId().asLong();
        var selectedDeckAction = deckService.getSelectedDeck(userId);
        if (selectedDeckAction.hasFailed()) return List.of();
        var deckEditing = selectedDeckAction.getResponse().getLeft();
        List<Card> cards = deckEditing.getEditedDeck().getCards();

        var eventValue = event.getFocusedOption().getValue();
        String input = eventValue.map(ApplicationCommandInteractionOptionValue::asString).orElse("");

        return cards.stream()
                .map(Card::getName)
                .distinct()
                .filter(x -> x.toLowerCase().contains(input.toLowerCase()))
                .map(x -> InputSuggestion.create(x, x))
                .toList();
    }
}
