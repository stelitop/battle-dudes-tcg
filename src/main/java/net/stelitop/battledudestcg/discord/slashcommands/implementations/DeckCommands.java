package net.stelitop.battledudestcg.discord.slashcommands.implementations;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import net.stelitop.battledudestcg.commons.pojos.ActionResult;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.CommandComponent;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.CommandEvent;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.SlashCommand;
import net.stelitop.battledudestcg.discord.slashcommands.framework.autocomplete.Autocompleted;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.CommandParam;
import net.stelitop.battledudestcg.discord.slashcommands.framework.definition.OptionalCommandParam;
import net.stelitop.battledudestcg.discord.slashcommands.implementations.autocomplete.DeckNameAutocomplete;
import net.stelitop.battledudestcg.discord.ui.DeckViewingUI;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.collection.UserCollection;
import net.stelitop.battledudestcg.game.database.entities.collection.CardDeck;
import net.stelitop.battledudestcg.game.database.entities.collection.DeckEditing;
import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
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
    @Autowired
    private CardRepository cardRepository;

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
            @CommandParam(name = "name", description = "The name of the deck.")
            @Autocompleted(implementation = DeckNameAutocomplete.class) String deckName
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
            @CommandParam(name = "name", description = "The name of the deck.")
            @Autocompleted(implementation = DeckNameAutocomplete.class) String deckName
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
                .block();

        Message message = event.getReply().block();
        var userCollection = collectionService.getUserCollection(userId);
        var deckEditingData = userCollection.getDeckEditing();
        deckEditingData.setEditedDeck(deck);
        deckEditingData.setLastEditTime(Date.from(Instant.now()));
        deckEditingData.setEditMsgId(message == null ? null : message.getId().asLong());
        deckEditingData.setEditMsgChannelId(message == null ? null : message.getChannelId().asLong());
        userCollectionRepository.save(userCollection);
        return Mono.empty();
    }

    @SlashCommand(
            name = "deck addcard",
            description = "Adds a new card to the currently selected deck."
    )
    public Mono<Void> addCardToDeck(
            @CommandEvent ChatInputInteractionEvent event,
            @CommandParam(name = "name", description = "The name of the card.") String cardName,
            @OptionalCommandParam(
                    name = "copies",
                    description = "The amount of copies to add to the deck. Default = 1.",
                    type = Long.class
            ) Optional<Long> copiesOpt
    ) {
        Optional<Card> cardOpt = cardRepository.findByNameIgnoreCase(cardName);
        if (cardOpt.isEmpty()) {
            return event.reply("There is no card with this name!")
                    .withEphemeral(true);
        }
        Card card = cardOpt.get();
        long copies = copiesOpt.orElse(1L);
        long userId = event.getInteraction().getUser().getId().asLong();
        var deckResult = deckService.getSelectedDeck(userId);
        if (deckResult.hasFailed()) {
            return event.reply(deckResult.errorMessage())
                    .withEphemeral(true);
        }
        DeckEditing deckEditing = deckResult.getResponse().getKey();
        CardDeck deck = deckEditing.getEditedDeck();
        Message message = deckResult.getResponse().getValue();
        UserCollection collection = collectionService.getUserCollection(userId);
        int availableCopies = collection.countCopiesOfCard(cardName) - deck.countCopiesOfCard(cardName);
        int allowedNewCopies = card.getRarity().allowedCopies() - deck.countCopiesOfCard(cardName);
        if (copies < 1 || copies > allowedNewCopies) {
            String msg = allowedNewCopies == 0 ? "You are not allowed to add more copies of this card!" :
                    "You can only add between 1 and " + allowedNewCopies + " new copies of this card!";
            return event.reply(msg)
                    .withEphemeral(true);
        }
        if (availableCopies < copies) {
            return event.reply("You don't have enough copies of this card! Available: " + availableCopies)
                    .withEphemeral(true);
        }
        for (int i = 0; i < copies; i++) {
            deck.getCards().add(card);
        }
        deckService.saveDeck(deck);
        event.deferReply().block();
        event.getReply().block().delete().block();
        var newUIMessage = deckViewingUI.getDeckViewingMessage(deck);
        message.edit()
                .withContentOrNull(newUIMessage.content().toOptional().orElse(null))
                .withEmbedsOrNull(newUIMessage.embeds().toOptional().orElse(null))
                .withComponentsOrNull(newUIMessage.components().toOptional().orElse(null)).block();
        return Mono.empty();
    }

    @SlashCommand(
            name = "deck removecard",
            description = "Removes a card from the currently selected deck."
    )
    public Mono<Void> removeCardToDeck(
            @CommandEvent ChatInputInteractionEvent event,
            @CommandParam(name = "name", description = "The name of the card.") String cardName,
            @OptionalCommandParam(
                    name = "copies",
                    description = "The amount of copies to remove from the deck. Default = 1.",
                    type = Long.class
            ) Optional<Long> copiesOpt
    ) {
        Optional<Card> cardOpt = cardRepository.findByNameIgnoreCase(cardName);
        if (cardOpt.isEmpty()) {
            return event.reply("There is no card with this name!")
                    .withEphemeral(true);
        }
        int copies = copiesOpt.orElse(1L).intValue();
        long userId = event.getInteraction().getUser().getId().asLong();
        var deckResult = deckService.getSelectedDeck(userId);
        if (deckResult.hasFailed()) {
            return event.reply(deckResult.errorMessage())
                    .withEphemeral(true);
        }
        DeckEditing deckEditing = deckResult.getResponse().getKey();
        CardDeck deck = deckEditing.getEditedDeck();
        Message message = deckResult.getResponse().getValue();
        UserCollection collection = collectionService.getUserCollection(userId);
        if (copies < 1 ) {
            return event.reply("The amount of copies removed must be at least one!")
                    .withEphemeral(true);
        }
        if (copies > deck.countCopiesOfCard(cardName)) {
            return event.reply("There aren't enough copies in the deck to remove!")
                    .withEphemeral(true);
        }
        for (int remaining = copies, i = deck.getCards().size() - 1; i >= 0 && remaining > 0; i--) {
            if (deck.getCards().get(i).getName().equalsIgnoreCase(cardName)) {
                deck.getCards().remove(i);
                i++;
                remaining--;
            }
        }
        deckService.saveDeck(deck);
        event.deferReply().block();
        event.getReply().block().delete().block();
        var newUIMessage = deckViewingUI.getDeckViewingMessage(deck);
        message.edit()
                .withContentOrNull(newUIMessage.content().toOptional().orElse(null))
                .withEmbedsOrNull(newUIMessage.embeds().toOptional().orElse(null))
                .withComponentsOrNull(newUIMessage.components().toOptional().orElse(null)).block();
        return Mono.empty();
    }
}
