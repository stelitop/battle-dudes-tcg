package net.stelitop.battledudestcg.discord.slashcommands.implementations.regularcommands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.battledudestcg.discord.framework.DiscordEventsComponent;
import net.stelitop.battledudestcg.discord.framework.InteractionEvent;
import net.stelitop.battledudestcg.discord.framework.commands.*;
import net.stelitop.battledudestcg.discord.ui.CardCollectionUI;
import net.stelitop.battledudestcg.discord.utils.ColorUtils;
import net.stelitop.battledudestcg.game.database.entities.collection.ChestOwnership;
import net.stelitop.battledudestcg.game.enums.ElementalType;
import net.stelitop.battledudestcg.game.services.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@DiscordEventsComponent
public class CollectionCommands {

    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private ColorUtils colorUtils;
    @Autowired
    private CardCollectionUI cardCollectionUI;

    @SlashCommand(
            name = "coins",
            description = "Gets the coins a user has. By default it shows your coins."
    )
    public Mono<Void> getUserCoins(
            @InteractionEvent ChatInputInteractionEvent event,
            @CommandParam(
                    name = "user",
                    description = "User to check the coins of. Defaults to self",
                    required = false
            ) User userOpt
    ) {
        User user = userOpt == null ? event.getInteraction().getUser() : userOpt;
        String username = user.getUsername();
        int coins = userProfileService.getProfile(user.getId().asLong()).getUserCollection().getCoins();
        return event.reply(username + " has " + coins + " coins!");
    }

    @SlashCommand(
            name = "collection chests",
            description = "A list of all chests that you own."
    )
    public Mono<Void> collectionChests(
            @InteractionEvent ChatInputInteractionEvent event,
            @CommandParam(
                    name = "user",
                    description = "The user you want to inspect, by default yourself.",
                    required = false
            ) User userOpt
    ) {
        User user = userOpt == null ? event.getInteraction().getUser() : userOpt;
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
            @InteractionEvent ChatInputInteractionEvent event,
            @CommandParam(
                    name = "cardtype",
                    description = "Limit cards shown only to a single type.",
                    required = false,
                    choices = {
                            @CommandParamChoice(name = "Dudes", value = "dude"),
                            @CommandParamChoice(name = "Items", value = "item"),
                            @CommandParamChoice(name = "Warps", value = "warp")
                    }
            ) String cardTypeOpt,
            @CommandParam(
                    name = "order",
                    description = "How to order the cards in the collection.",
                    required = false,
                    choices = {
                            @CommandParamChoice(name = "Name", value = "name"),
                            @CommandParamChoice(name = "Elemental Types", value = "eltypes"),
                            @CommandParamChoice(name = "Rarity", value = "rarity")
                    }
            ) String orderingOpt
    ) {
        MessageCreateSpec message = cardCollectionUI.getCardCollectionMessage(CardCollectionUI
                .Model.builder()
                .page(1)
                .userId(event.getInteraction().getUser().getId().asLong())
                .cardType(cardTypeOpt == null ? "all" : cardTypeOpt)
                .ordering(orderingOpt == null ? "default" : orderingOpt)
                .build(), event.getInteraction().getUser());

        return event.reply()
                .withContent(message.content())
                .withEmbeds(message.embeds())
                .withComponents(message.components());
    }
}
