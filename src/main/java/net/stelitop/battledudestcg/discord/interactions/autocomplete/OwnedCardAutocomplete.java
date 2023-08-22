package net.stelitop.battledudestcg.discord.interactions.autocomplete;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import net.stelitop.battledudestcg.discord.framework.autocomplete.AutocompletionExecutor;
import net.stelitop.battledudestcg.discord.framework.autocomplete.InputSuggestion;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.collection.CardOwnership;
import net.stelitop.battledudestcg.game.services.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OwnedCardAutocomplete implements AutocompletionExecutor {

    @Autowired
    private CollectionService collectionService;

    @Override
    public List<InputSuggestion> execute(ChatInputAutoCompleteEvent event) {
        long userId = event.getInteraction().getUser().getId().asLong();
        var collection = collectionService.getUserCollection(userId);
        var eventValue = event.getFocusedOption().getValue();
        String input = eventValue.map(ApplicationCommandInteractionOptionValue::asString).orElse("");

        return collection.getOwnedCards().stream()
                .filter(x -> x.getOwnedCopies() > 0)
                .map(CardOwnership::getCard)
                .map(Card::getName)
                .filter(x -> x.toLowerCase().contains(input.toLowerCase()))
                .map(x -> InputSuggestion.create(x, x))
                .toList();
    }
}
