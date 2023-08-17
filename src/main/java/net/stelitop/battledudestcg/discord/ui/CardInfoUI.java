package net.stelitop.battledudestcg.discord.ui;

import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.battledudestcg.commons.pojos.ActionResult;
import net.stelitop.battledudestcg.discord.DiscordBotSettings;
import net.stelitop.battledudestcg.discord.utils.ColorUtils;
import net.stelitop.battledudestcg.discord.utils.EmojiUtils;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.cards.DudeCard;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.enums.DudeStat;
import net.stelitop.battledudestcg.game.enums.ElementalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CardInfoUI {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DiscordBotSettings discordBotSettings;
    @Autowired
    private ColorUtils colorUtils;
    @Autowired
    private EmojiUtils emojiUtils;

    public MessageCreateSpec getCardInfoMessage(Card card) {

        if (card instanceof DudeCard dude) {
            return getDudeInfoMessage(dude);
        } else {
            return getGenericCardMessage(card);
        }
    }

    /**
     * Creates a custom embed for a card of type Dude.
     *
     * @param dude The dude card.
     * @return The embed.
     */
    private MessageCreateSpec getDudeInfoMessage(DudeCard dude) {

        var embed = EmbedCreateSpec.builder()
                .title(dude.getFormattedId() + " - " + dude.getName())
                .thumbnail(dude.getArtUrl())
                .color(colorUtils.getColor(dude.getTypes()))
                .addField(getCollectionInfoField(dude, false))
                .addField(getEvolutionsInfoField(dude, false))
                .addField("Type Info",
                        dude.getTypes().stream()
                                .map(type -> emojiUtils.getEmojiString(type) + " " + type.toString().toUpperCase())
                                .collect(Collectors.joining(" - ")) +
                                (dude.getResistances().isEmpty() ? "" : "\nResistant to " +
                                        dude.getResistances().stream().map(x -> emojiUtils.getEmojiString(x) + " " + x.toString().toUpperCase())
                                                .collect(Collectors.joining(" - "))) +
                                (dude.getWeaknesses().isEmpty() ? "" : "\nWeak to " +
                                        dude.getWeaknesses().stream().map(x -> emojiUtils.getEmojiString(x) + " " + x.toString().toUpperCase())
                                                .collect(Collectors.joining(" - "))),
                        true)
                .addField("Statistics",
                        "Cost: " + Objects.requireNonNull(ElementalType.parseString(dude.getCost())).stream()
                                .map(emojiUtils::getEmojiString).collect(Collectors.joining(""))
                                + "\n" + emojiUtils.getEmojiString(DudeStat.Health) + " Health: " + dude.getHealth()
                                + "\n" + emojiUtils.getEmojiString(DudeStat.Offense) + " Offense: " + dude.getOffense()
                                + "\n" + emojiUtils.getEmojiString(DudeStat.Defence) + " Defence: " + dude.getDefence(),
                        true)
                .addField(getEffectInfoField(dude, false))
                .addField("\u200B",
                        (dude.getFlavorText() == null ? "" : "\n\n*" + dude.getFlavorText() + "*"), false)
                .footer("Art by " + String.join(", ", dude.getArtists()), null);

        return MessageCreateSpec.builder()
                .addEmbed(embed.build())
                .build();
    }

    /**
     * Creates a generalised embed for any type of card.
     *
     * @param card The card to make an embed for.
     * @return The embed.
     */
    private MessageCreateSpec getGenericCardMessage(Card card) {
        var embed = EmbedCreateSpec.builder()
                .title(card.getName())
                .thumbnail(card.getArtUrl())
                .color(colorUtils.getColor(card.getTypes()))
                .addField(getCollectionInfoField(card, false))
                .addField("Type Info",
                        card.getTypes().stream()
                                .map(type -> emojiUtils.getEmojiString(type) + " " + type.toString().toUpperCase())
                                .collect(Collectors.joining(" - ")),
                        true)
                .addField("\u200B",
                        (card.getFlavorText() == null ? "" : "\n\n*" + card.getFlavorText() + "*"), false)
                .addField(getEffectInfoField(card, false))
                .footer("Art by " + String.join(", ", card.getArtists()), null);

        return MessageCreateSpec.builder()
                .addEmbed(embed.build())
                .build();
    }

    /**
     * Creates a field with the information about how to collect a card.
     *
     * @param card The card to create the field for.
     * @param inline Whether the field to be inline or not.
     * @return The field
     */
    private EmbedCreateFields.Field getCollectionInfoField(Card card, boolean inline) {
        var locationsMsg = card.getChestSources().isEmpty() ? " None" : "\n" + card.getChestSources()
                .stream()
                .distinct()
                .map(Chest::getName)
                .map(x -> discordBotSettings.getChannelChestLocations().get(x))
                .filter(Objects::nonNull)
                .map(x -> "> <#" + x + ">")
                .collect(Collectors.joining("\n"));

        return EmbedCreateFields.Field.of(
                "Collection Info",
                "Locations:" + locationsMsg + "\nRarity: " + card.getRarity(),
                inline
        );
    }

    /**
     * Creates a field about the evolution info of a dude card.
     *
     * @param dude The dude for which to create the field.
     * @param inline Whether the field is inline or not.
     * @return The field
     */
    private EmbedCreateFields.Field getEvolutionsInfoField(DudeCard dude, boolean inline) {
        String description = "Stage: " + dude.getStage() +
                "\nEvolves From: " + (dude.getPreviousEvolutions() != null && !dude.getPreviousEvolutions().isEmpty() ?
                String.join(", ", dude.getPreviousEvolutions()) : "N/A") +
                "\nEvolves Into: " + (dude.getNextEvolutions() != null && !dude.getNextEvolutions().isEmpty() ?
                String.join(", ", dude.getNextEvolutions()) : "N/A");

        return EmbedCreateFields.Field.of("Evolution Info", description, false);
    }

    private EmbedCreateFields.Field getEffectInfoField(Card card, boolean inline) {
        ActionResult<String> parsedEffectTextResult = emojiUtils.formatCardTextWithSpecialStrings(card.getEffectText());
        String text = parsedEffectTextResult.isSuccessful() ? parsedEffectTextResult.getResponse() : card.getEffectText();

        if (parsedEffectTextResult.hasFailed()) {
            LOGGER.error("Could not parse the effect text of card \"" + card.getName() + "\"!"
                    + "   Error: " + parsedEffectTextResult.errorMessage());
        }

        return EmbedCreateFields.Field.of("Effect", text.isEmpty() ? "(none)" : text, inline);
    }
}
