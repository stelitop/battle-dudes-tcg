package net.stelitop.battledudestcg.discord.slashcommands.implementations.devcommands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import net.stelitop.battledudestcg.discord.framework.definition.DiscordEventsComponent;
import net.stelitop.battledudestcg.discord.framework.definition.InteractionEvent;
import net.stelitop.battledudestcg.discord.framework.definition.CommandParam;
import net.stelitop.battledudestcg.discord.framework.definition.SlashCommand;
import net.stelitop.battledudestcg.discord.slashcommands.implementations.requirements.RequireAdmin;
import net.stelitop.battledudestcg.game.database.entities.collection.CardOwnership;
import net.stelitop.battledudestcg.game.database.entities.collection.UserCollectionCardKey;
import net.stelitop.battledudestcg.game.database.repositories.CardOwnershipRepository;
import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
import net.stelitop.battledudestcg.game.services.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.stream.StreamSupport;

@DiscordEventsComponent
public class GrantCardsCommands {

    @Autowired
    private CollectionService collectionService;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardOwnershipRepository cardOwnershipRepository;

    @RequireAdmin
    @SlashCommand(
            name = "dev givecollection",
            description = "Gives a user the entire collection of cards with every copy."
    )
    public Mono<Void> giveUserEntireCollection(
            @InteractionEvent ChatInputInteractionEvent event,
            @CommandParam(
                    name = "user",
                    description = "The user to grant the collection"
            ) User user
    ) {
        var collection = collectionService.getUserCollection(user.getId().asLong());
        var newOwnerships = StreamSupport.stream(cardRepository.findAll().spliterator(), false)
                .map(x -> CardOwnership.builder()
                        .card(x)
                        .userCollection(collection)
                        .ownedCopies(x.getRarity().allowedCopies())
                        .id(new UserCollectionCardKey(collection.getCollectionId(), x.getCardId()))
                        .build())
                .toList();
        cardOwnershipRepository.saveAll(newOwnerships);
        return event.reply("Collection granted!").withEphemeral(true);
    }
}
