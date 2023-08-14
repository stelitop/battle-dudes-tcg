package net.stelitop.battledudestcg.discord.ui;

import com.google.common.collect.Lists;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.collection.CardDeck;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DeckViewingUI {

    public MessageCreateSpec getDeckViewingMessage(CardDeck deck) {
        Map<String, Integer> cardNameToCount = new HashMap<>();
        Map<String, Card> cardNameToCard = new HashMap<>();

        for (var card : deck.getCards()) {
            String nameLower = card.getName().toLowerCase();
            cardNameToCount.put(nameLower, cardNameToCount.getOrDefault(nameLower, 0) + 1);
            cardNameToCard.put(nameLower, card);
        }

        List<String> allCardNames = new ArrayList<>(cardNameToCard.keySet());
        allCardNames.sort(Comparator.comparing(x -> x));
        List<List<String>> cardNamePartitions = Lists.partition(allCardNames, 20);
        List<EmbedCreateFields.Field> embedFields = new ArrayList<>();
        cardNamePartitions.forEach(x -> embedFields.add(createFieldFromNames(x, cardNameToCount, cardNameToCard)));

        return MessageCreateSpec.builder()
                .addEmbed(EmbedCreateSpec.builder()
                        .title("Deck: \"" + deck.getName() + "\"")
                        .description("## Cards")
                        .addAllFields(embedFields)
                        .color(Color.CYAN)
                        .thumbnail("https://static.thenounproject.com/png/219525-200.png")
                        .build())
                .build();
    }

    private EmbedCreateFields.Field createFieldFromNames(
            List<String> cardNames,
            Map<String, Integer> cardNameToCount,
            Map<String, Card> cardNameToCard
    ) {
        String description = cardNames.stream()
                .map(n -> "> " + cardNameToCard.get(n).getName() + " x" + cardNameToCount.get(n))
                .collect(Collectors.joining("\n"));

        description = description.isBlank() ? "(empty)" : description;

        return EmbedCreateFields.Field.of("\u200B", description, true);
    }
}
