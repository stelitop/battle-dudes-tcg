package net.stelitop.battledudestcg.discord.interactions.autocomplete;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import net.stelitop.battledudestcg.discord.framework.autocomplete.AutocompletionExecutor;
import net.stelitop.battledudestcg.discord.framework.autocomplete.InputSuggestion;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.StreamSupport;

@Component
public class CardNameAutocomplete implements AutocompletionExecutor {

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardNameFilterer cardNameFilterer;
    @Override
    public List<InputSuggestion> execute(ChatInputAutoCompleteEvent event) {

        var focusedOptionValue = event.getFocusedOption().getValue();
        String filter = focusedOptionValue.isEmpty() ? "" : focusedOptionValue.get().asString().toLowerCase();

        List<Card> cards = cardNameFilterer.applySpecialFilter(filter,
                StreamSupport.stream(cardRepository.findAll().spliterator(), true).toList());

        return cards.stream()
                .map(Card::getName)
                .map(x -> InputSuggestion.create(x, x))
                .toList();
    }
}
