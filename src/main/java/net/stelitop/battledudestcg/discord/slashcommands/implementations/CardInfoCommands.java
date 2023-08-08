package net.stelitop.battledudestcg.discord.slashcommands.implementations;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import net.stelitop.battledudestcg.discord.utils.ColorUtils;
import net.stelitop.battledudestcg.discord.utils.EmojiUtils;
import net.stelitop.battledudestcg.discord.DiscordBotSettings;
import net.stelitop.battledudestcg.discord.slashcommands.annotations.*;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.cards.DudeCard;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
import net.stelitop.battledudestcg.game.enums.DudeStat;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@CommandComponent
public class CardInfoCommands {

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private EmojiUtils emojiUtils;
    @Autowired
    private ColorUtils colorUtils;
    @Autowired
    private DiscordBotSettings discordBotSettings;

//    @SlashCommand(
//            name = "dude",
//            description = "Displays information about a dude."
//    )
//    public Mono<Void> dudeInfo(
//            @CommandEvent ChatInputInteractionEvent event,
//            @CommandParam(name = "name", description = "The name of the dude.") String name
//    ) {
//        Optional<Card> dudeOpt = cardRepository.findByNameIgnoreCase(name);
//        if (dudeOpt.isEmpty() || !(dudeOpt.get() instanceof DudeCard dude)) {
//            return event.reply()
//                    .withContent("No matching dudes!")
//                    .withEphemeral(true);
//        }
//
//        EmbedCreateSpec embed = getDudeInfoEmbed(dude);
//        return event.reply()
//                .withEmbeds(embed);
//    }

    @SlashCommand(
            name = "info card",
            description = "Displays information about a card."
    )
    public Mono<Void> cardInfo(
            @CommandEvent ChatInputInteractionEvent event,
            @CommandParam(name = "name", description = "The name of the card.") String name
    ) {
        Optional<Card> cardOpt = cardRepository.findByNameIgnoreCase(name);
        if (cardOpt.isEmpty()) {
            return event.reply("No matching cards!")
                    .withEphemeral(true);
        }
        Card card = cardOpt.get();
        EmbedCreateSpec embed;
        if (card instanceof DudeCard dude) {
            embed = getDudeInfoEmbed(dude);
        } else {
            embed = getGenericCardEmbed(card);
        }

        return event.reply()
                .withEmbeds(embed);
    }

    public EmbedCreateSpec getDudeInfoEmbed(DudeCard dude) {
        var locationsMsg = dude.getChestSources().isEmpty() ? "None" : dude.getChestSources()
                .stream()
                .distinct()
                .map(Chest::getName)
                .map(x -> discordBotSettings.getChannelChestLocations().get(x))
                .filter(Objects::nonNull)
                .map(x -> "> <#" + x + ">")
                .collect(Collectors.joining("\n"));

        var embed = EmbedCreateSpec.builder()
                .title(dude.getFormattedId() + " - " + dude.getName())
                .thumbnail(dude.getArtUrl())
                .color(colorUtils.getColor(dude.getTypes()))
                .addField("Collection Info",
                        "Locations:\n" + locationsMsg
                                + "\nRarity: " + dude.getRarity(),
                        //+ "\nTotal Collected: " + dude.getUsersThatOwn().size(),
                        false)
                .addField("Evolution Info",
                        "Stage: " + dude.getStage() +
                                "\nEvolves From: " + (dude.getPreviousEvolutions() != null && !dude.getPreviousEvolutions().isEmpty() ?
                                String.join(", ", dude.getPreviousEvolutions()) : "N/A") +
                                "\nEvolves Into: " + (dude.getNextEvolutions() != null && !dude.getNextEvolutions().isEmpty() ?
                                String.join(", ", dude.getNextEvolutions()) : "N/A"),
                        false)
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
                        emojiUtils.getEmojiString(DudeStat.Health) + " Health: " + dude.getHealth() +
                                "\n" + emojiUtils.getEmojiString(DudeStat.Offense) + " Offense: " + dude.getOffense() +
                                "\n" + emojiUtils.getEmojiString(DudeStat.Defence) + " Defense: " + dude.getDefense(),
                        true)
                .addField("Effect", dude.getEffectText().isEmpty() ? "(none)" : dude.getEffectText(), false)
                .addField("\u200B",
                        (dude.getFlavorText() == null ? "" : "\n\n*" + dude.getFlavorText() + "*"), false)
                .footer("Art by " + String.join(", ", dude.getArtists()), null);

        return embed.build();
    }

    public EmbedCreateSpec getGenericCardEmbed(Card card) {
        var locationsMsg = card.getChestSources().isEmpty() ? "None" : card.getChestSources()
                .stream()
                .distinct()
                .map(Chest::getName)
                .map(x -> discordBotSettings.getChannelChestLocations().get(x))
                .filter(Objects::nonNull)
                .map(x -> "> <#" + x + ">")
                .collect(Collectors.joining("\n"));

        var embed = EmbedCreateSpec.builder()
                .title(card.getName())
                .thumbnail(card.getArtUrl())
                .color(colorUtils.getColor(card.getTypes()))
                .addField("Collection Info",
                        "Locations:\n" + locationsMsg
                                + "\nRarity: " + card.getRarity(),
                        //+ "\nTotal Collected: " + dude.getUsersThatOwn().size(),
                        false)
                .addField("Type Info",
                        card.getTypes().stream()
                                .map(type -> emojiUtils.getEmojiString(type) + " " + type.toString().toUpperCase())
                                .collect(Collectors.joining(" - ")),
                        true)
                .addField("Effect", card.getEffectText().isEmpty() ? "(none)" : card.getEffectText(), false)
                .addField("\u200B",
                        (card.getFlavorText() == null ? "" : "\n\n*" + card.getFlavorText() + "*"), false)
                .footer("Art by " + String.join(", ", card.getArtists()), null);

        return embed.build();
    }

}
