package net.stelitop.battledudestcg.discord.slashcommands.implementations;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import net.stelitop.battledudestcg.discord.slashcommands.base.definition.CommandComponent;
import net.stelitop.battledudestcg.discord.slashcommands.base.definition.CommandEvent;
import net.stelitop.battledudestcg.discord.slashcommands.base.definition.SlashCommand;
import net.stelitop.battledudestcg.discord.slashcommands.base.definition.params.CommandParam;
import net.stelitop.battledudestcg.discord.slashcommands.base.definition.params.OptionalCommandParam;
import net.stelitop.battledudestcg.game.database.entities.chests.ChannelChest;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.UserCollection;
import net.stelitop.battledudestcg.game.database.repositories.UserCollectionRepository;
import net.stelitop.battledudestcg.game.services.ChestService;
import net.stelitop.battledudestcg.game.services.CollectionService;
import net.stelitop.battledudestcg.game.services.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.Optional;

@CommandComponent
public class BuyCommands {

    @Autowired
    private ChestService chestService;
    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private CollectionService collectionService;
    @Autowired
    private UserCollectionRepository userCollectionRepository;

    @SlashCommand(
            name = "buy chest",
            description = "Buys a specific chest. One chest costs 100 coins."
    )
    public Mono<Void> buyChestCommand(
            @CommandEvent ChatInputInteractionEvent event,
            @CommandParam(name = "name", description = "The name of the chest") String name,
            @OptionalCommandParam(name = "amount", description = "Amount of chests to buy. Default is 1. Max is 50.", type = Long.class) Optional<Long> amountOpt
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
        int chestsAmount = amountOpt.orElse(1L).intValue();
        if (chestsAmount < 1 || chestsAmount > 50) {
            return event.reply("Invalid chest amount! Must be between 1 and 50.")
                    .withEphemeral(true);
        }
        final long userId = event.getInteraction().getUser().getId().asLong();
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
