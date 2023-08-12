package net.stelitop.battledudestcg.discord.slashcommands.implementations;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.battledudestcg.commons.pojos.ActionResult;
import net.stelitop.battledudestcg.discord.slashcommands.base.definition.CommandComponent;
import net.stelitop.battledudestcg.discord.slashcommands.base.definition.CommandEvent;
import net.stelitop.battledudestcg.discord.slashcommands.base.definition.SlashCommand;
import net.stelitop.battledudestcg.discord.slashcommands.base.definition.params.CommandParam;
import net.stelitop.battledudestcg.discord.slashcommands.base.definition.params.OptionalCommandParam;
import net.stelitop.battledudestcg.discord.ui.DeckViewingUI;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.decks.CardDeck;
import net.stelitop.battledudestcg.game.database.repositories.UserCollectionRepository;
import net.stelitop.battledudestcg.game.services.CollectionService;
import net.stelitop.battledudestcg.game.services.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.time.Instant;
import java.util.Optional;

@CommandComponent
public class DeckCommands {

    @Autowired
    private DeckService deckService;
    @Autowired
    private DeckViewingUI deckViewingUI;
    @Autowired
    private CollectionService collectionService;
    @Autowired
    private UserCollectionRepository userCollectionRepository;

    @SlashCommand(
            name = "deck create",
            description = "Creates a new empty deck."
    )
    public Mono<Void> createDeck(
            @CommandEvent ChatInputInteractionEvent event,
            @CommandParam(name = "name", description = "The name of the deck. Must be unique.") String deckName
    ) {
        long userId = event.getInteraction().getUser().getId().asLong();
        ActionResult<CardDeck> actionResult = deckService.createNewDeck(userId, deckName);
        String message = actionResult.isSuccessful() ? "Successfully created a new deck!" : actionResult.errorMessage();
        return event.reply(message)
                .withEphemeral(true);
    }

    @SlashCommand(
            name = "deck view",
            description = "Displays a deck in the public chat."
    )
    public Mono<Void> viewDeck(
            @CommandEvent ChatInputInteractionEvent event,
            @CommandParam(name = "name", description = "The name of the deck.") String deckName
    ) {
        // TODO: Change the way the deck is selected.
        var decks = deckService.getDecksOfUser(event.getInteraction().getUser().getId().asLong());
        var deckOpt = decks.stream().filter(x -> x.getName().equalsIgnoreCase(deckName)).findFirst();
        if (deckOpt.isEmpty()) {
            return event.reply("No deck with this name was found!")
                    .withEphemeral(true);
        }
        var msg = deckViewingUI.getDeckViewingMessage(deckOpt.get());
        return event.reply()
                .withContent(msg.content())
                .withEmbeds(msg.embeds())
                .withComponents(msg.components());
    }

    @SlashCommand(
            name = "deck edit",
            description = "Opens a deck and allows you to edit it with other commands."
    )
    public Mono<Void> editDeck(
            @CommandEvent ChatInputInteractionEvent event,
            @CommandParam(name = "name", description = "The name of the deck.") String deckName
    ) {
        // TODO: Change the way the deck is selected.
        var decks = deckService.getDecksOfUser(event.getInteraction().getUser().getId().asLong());
        var deckOpt = decks.stream().filter(x -> x.getName().equalsIgnoreCase(deckName)).findFirst();
        if (deckOpt.isEmpty()) {
            return event.reply("No deck with this name was found!")
                    .withEphemeral(true);
        }
        var deck = deckOpt.get();
        long userId = event.getInteraction().getUser().getId().asLong();
        var msg = deckViewingUI.getDeckViewingMessage(deck);

        event.reply().withContent(msg.content())
                .withEmbeds(msg.embeds())
                .withComponents(msg.components())
                .withEphemeral(true).block();

        Message message = event.getReply().block();
        var userCollection = collectionService.getUserCollection(userId);
        var deckEditingData = userCollection.getDeckEditing();
        deckEditingData.setEditedDeck(deck);
        deckEditingData.setLastEditTime(Date.from(Instant.now()));
        deckEditingData.setEditMsgId(message == null ? null : message.getId().asLong());
        userCollectionRepository.save(userCollection);
        return Mono.empty();
    }

    @SlashCommand(
            name = "deck addcard",
            description = "Adds a new card to the currently selected deck."
    )
    public Mono<Void> addCardToDeck(
            @CommandEvent ChatInputInteractionEvent event,
            @CommandParam(name = "name", description = "The name of the card.") String deckName,
            @OptionalCommandParam(
                    name = "copies",
                    description = "The amount of copies to add to the deck. Default = 1.",
                    type = Long.class
            ) Optional<Long> copiesOpt
    ) {
        return Mono.empty();
    }

    @SlashCommand(
            name = "deck removecard",
            description = "Removes a card from the currently selected deck."
    )
    public Mono<Void> removeCardToDeck(
            @CommandEvent ChatInputInteractionEvent event,
            @CommandParam(name = "name", description = "The name of the card.") String deckName,
            @OptionalCommandParam(
                    name = "copies",
                    description = "The amount of copies to remove from the deck. Default = 1.",
                    type = Long.class
            ) Optional<Long> copiesOpt
    ) {
        return Mono.empty();
    }
}
