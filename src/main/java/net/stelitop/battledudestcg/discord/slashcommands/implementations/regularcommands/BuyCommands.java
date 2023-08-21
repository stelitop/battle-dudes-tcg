package net.stelitop.battledudestcg.discord.slashcommands.implementations.regularcommands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import net.stelitop.battledudestcg.discord.framework.DiscordEventsComponent;
import net.stelitop.battledudestcg.discord.framework.InteractionEvent;
import net.stelitop.battledudestcg.discord.framework.commands.CommandParam;
import net.stelitop.battledudestcg.discord.framework.commands.SlashCommand;
import net.stelitop.battledudestcg.discord.framework.convenience.EventUserId;
import net.stelitop.battledudestcg.discord.slashcommands.implementations.autocomplete.ChestNameAutocomplete;
import net.stelitop.battledudestcg.game.database.entities.chests.ChannelChest;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.entities.collection.UserCollection;
import net.stelitop.battledudestcg.game.database.repositories.UserCollectionRepository;
import net.stelitop.battledudestcg.game.services.ChestService;
import net.stelitop.battledudestcg.game.services.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

@DiscordEventsComponent
public class BuyCommands {

    @Autowired
    private ChestService chestService;
    @Autowired
    private CollectionService collectionService;
    @Autowired
    private UserCollectionRepository userCollectionRepository;

    @SlashCommand(
            name = "buy chest",
            description = "Buys a specific chest. One chest costs 100 coins."
    )
    public Mono<Void> buyChestCommand(
            @InteractionEvent ChatInputInteractionEvent event,
            @EventUserId long userId,
            @CommandParam(
                    name = "name",
                    description = "The name of the chest",
                    autocomplete = ChestNameAutocomplete.class
            ) String name,
            @CommandParam(
                    name = "amount",
                    description = "Amount of chests to buy. Default is 1. Max is 50.",
                    required = false
            ) Long amountOpt
    ) {
        Chest chest = chestService.getChest(name);
        if (chest == null) {
            return event.reply("There is no chests of this name!")
                    .withEphemeral(true);
        }
        if (!(chest instanceof ChannelChest)) {
            return event.reply("You are not allowed to purchase this chest!")
                    .withEphemeral(true);
        }
        int chestsAmount = (int)(amountOpt == null ? 1L : amountOpt);
        if (chestsAmount < 1 || chestsAmount > 50) {
            return event.reply("Invalid chest amount! Must be between 1 and 50.")
                    .withEphemeral(true);
        }
        UserCollection collection = collectionService.getUserCollection(userId);
        final int chestCost = 100;
        final int totalCost = chestCost * chestsAmount;
        if (collection.getCoins() < totalCost) {
            return event.reply("You do not have enough coins for this purchase! Required: " + totalCost)
                    .withEphemeral(true);
        }
        collection.setCoins(collection.getCoins() - totalCost);
        userCollectionRepository.save(collection);
        collectionService.giveUserChests(userId, chest, chestsAmount);
        return event.reply("Successfully bought " + chestsAmount + " " + chest.getName() + "!")
                .withEphemeral(true);
    }
}
