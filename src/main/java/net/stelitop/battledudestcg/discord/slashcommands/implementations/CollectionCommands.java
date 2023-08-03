package net.stelitop.battledudestcg.discord.slashcommands.implementations;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.possible.Possible;
import net.stelitop.battledudestcg.discord.listeners.buttons.CardCollectionPageButtonListener;
import net.stelitop.battledudestcg.discord.slashcommands.annotations.*;
import net.stelitop.battledudestcg.discord.utils.ColorUtils;
import net.stelitop.battledudestcg.discord.utils.EmojiUtils;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.CardOwnership;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.ChestOwnership;
import net.stelitop.battledudestcg.game.enums.ElementalType;
import net.stelitop.battledudestcg.game.services.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CommandComponent
public class CollectionCommands {

    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private ColorUtils colorUtils;
    @Autowired
    private EmojiUtils emojiUtils;
    @Autowired
    private CardCollectionPageButtonListener ccpgl;

    @SlashCommand(
            name = "coins",
            description = "Gets the coins a user has. By default it shows your coins."
    )
    public Mono<Void> getUserCoins(
            @CommandEvent ChatInputInteractionEvent event,
            @OptionalCommandParam(
                    name = "user",
                    description = "User to check the coins of. Defaults to self",
                    type = User.class
            ) Optional<User> userOpt
    ) {
        User user = userOpt.orElse(event.getInteraction().getUser());
        String username = user.getUsername();
        int coins = userProfileService.getProfile(user.getId().asLong()).getUserCollection().getCoins();
        return event.reply()
                .withContent(username + " has " + coins + " coins!");
    }

    @SlashCommand(
            name = "collection chests",
            description = "A list of all chests that you own."
    )
    public Mono<Void> collectionChests(
            @CommandEvent ChatInputInteractionEvent event,
            @OptionalCommandParam(
                    name = "user",
                    description = "The user you want to inspect, by default yourself.",
                    type = User.class
            ) Optional<User> userOpt
    ) {
        User user = userOpt.orElse(event.getInteraction().getUser());
        String username = user.getUsername();
        var profile = userProfileService.getProfile(user.getId().asLong());
        List<ChestOwnership> chestOwnerships = profile.getUserCollection().getOwnedChests().stream()
                .filter(x -> x.getCount() > 0)
                .toList();

        String description = chestOwnerships.stream()
                .map(co -> co.getChest().getName() + " x" + co.getCount())
                .collect(Collectors.joining("\n"));

        return event.reply()
                .withEmbeds(EmbedCreateSpec.builder()
                        .title(username + "'s Chests")
                        .description(description)
                        .color(colorUtils.getColor(ElementalType.Neutral))
                        .build());
    }

    @SlashCommand(
            name = "collection cards",
            description = "A list of all cards that you own."
    )
    public Mono<Void> collectionCards(
            @CommandEvent ChatInputInteractionEvent event,
            @OptionalCommandParam(
                    name = "user",
                    description = "The user you want to inspect, by default yourself.",
                    type = User.class
            ) Optional<User> userOpt,
            @OptionalCommandParam(
                    name = "cardtype",
                    description = "Limit cards shown only to a single type.",
                    type = String.class,
                    choices = {
                            @CommandParamChoice(name = "Dudes", value = "dude"),
                            @CommandParamChoice(name = "Items", value = "item"),
                            @CommandParamChoice(name = "Warps", value = "warp")
                    }
            ) Optional<String> cardType
    ) {
        MessageCreateSpec message = ccpgl.getCardCollectionMessage(CardCollectionPageButtonListener
                .CollectionUiModel.builder()
                .page(1)
                .userId(event.getInteraction().getUser().getId().asLong())
                .cardType(cardType.orElse("all"))
                .build(), event.getInteraction().getUser());

        return event.reply()
                .withContent(message.content())
                .withEmbeds(message.embeds())
                .withComponents(message.components());
    }
}
