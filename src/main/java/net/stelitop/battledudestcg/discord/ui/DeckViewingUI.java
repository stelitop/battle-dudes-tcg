package net.stelitop.battledudestcg.discord.ui;

import com.google.common.collect.Lists;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import net.stelitop.battledudestcg.discord.utils.EmojiUtils;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.collection.CardDeck;
import net.stelitop.battledudestcg.game.enums.ElementalType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeckViewingUI {

    @Autowired
    private EmojiUtils emojiUtils;

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
        if (embedFields.size() > 0) {
            embedFields.set(0, EmbedCreateFields.Field.of(
                    "Cards: " + deck.getCards().size() + "/60",
                    embedFields.get(0).value(),
                    true));
        }

        return MessageCreateSpec.builder()
                .addEmbed(EmbedCreateSpec.builder()
                        .title("Deck: \"" + deck.getName() + "\"")
                        .description(getDescription(deck))
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

    private String getDescription(CardDeck deck) {
        List<ElementalType> presentTypes = deck.getCards().stream()
                .flatMap(x -> x.getTypes().stream())
                .distinct()
                .filter(x -> x != ElementalType.Neutral && x != ElementalType.Ultimate)
                .toList();

        String presentTypesMsg = presentTypes.stream()
                .map(emojiUtils::getEmojiString)
                .collect(Collectors.joining());

        return "Elements: " + presentTypesMsg;
    }
}
