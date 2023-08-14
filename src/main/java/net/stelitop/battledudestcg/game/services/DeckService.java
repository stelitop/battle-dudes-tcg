package net.stelitop.battledudestcg.game.services;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import net.stelitop.battledudestcg.commons.pojos.ActionResult;
import net.stelitop.battledudestcg.game.database.entities.collection.CardDeck;
import net.stelitop.battledudestcg.game.database.entities.collection.DeckEditing;
import net.stelitop.battledudestcg.game.database.repositories.DeckRepository;
import net.stelitop.battledudestcg.game.database.repositories.UserCollectionRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Service with functionality related to the users' decks.
 */
@Service
public class DeckService {

    public static final long DECK_EDIT_MESSAGE_TIMEOUT = 1000 * 60 * 15;

    @Autowired
    private DeckRepository deckRepository;
    @Autowired
    private UserCollectionRepository userCollectionRepository;
    @Autowired
    private CollectionService collectionService;
    @Autowired
    private GatewayDiscordClient client;

    /**
     * Gets all decks belonging to a given user, by the user's id.
     *
     * @param userId The id of the user.
     * @return The decks owned by the user.
     */
    public @NotNull List<CardDeck> getDecksOfUser(long userId) {
        return deckRepository.findCardDecksByUserId(userId);
    }

    /**
     * Gets the names of all decks belong to a given user.
     *
     * @param userId The id of the user.
     * @return The names of the decks.
     */
    public @NotNull List<String> getUserDeckNames(long userId) {
        return deckRepository.findNamesOfDecksOfUser(userId);
    }

    /**
     * Creates a new empty deck for a user.
     *
     * @param userId The id of the user.
     * @param deckName The name for the new deck. All deck names must be unique
     *     when converted to lowercase.
     * @return The new deck object, or null if the deck couldn't be created.
     */
    public @NotNull ActionResult<CardDeck> createNewDeck(long userId, @NotNull String deckName) {
        List<String> existingNames = getUserDeckNames(userId);
        String lowercaseDeckName = deckName.toLowerCase();
        if (existingNames.stream().map(String::toLowerCase).anyMatch(lowercaseDeckName::equals)) {
            return ActionResult.fail("There is already an existing deck with this name!");
        }
        //System.out.println(deckRepository.findCardDecksByUserId(userId));

        var userCollection = userCollectionRepository.findByUserId(userId).get();
        System.out.println(userCollection);
        CardDeck deck = new CardDeck();
        deck.setName(deckName);
        deck.setCards(new ArrayList<>());
        deck.setUserCollection(userCollection);
        userCollection.getDecks().add(deck);
        System.out.println(deck);
        CardDeck savedDeck = deckRepository.save(deck);

        return ActionResult.success(savedDeck);
    }

    public ActionResult<Pair<DeckEditing, Message>> getSelectedDeck(long userId) {
        var collection = collectionService.getUserCollection(userId);
        var deck = collection.getDeckEditing().getEditedDeck();
        if (deck == null) {
            return ActionResult.fail("There is no deck selected! Use /deck edit");
        }
        var deckEditing = collection.getDeckEditing();
        var lastEditDate = deckEditing.getLastEditTime();
        if (Instant.now().toEpochMilli() - lastEditDate.toInstant().toEpochMilli() >= DECK_EDIT_MESSAGE_TIMEOUT) {
            return ActionResult.fail("Your last edit message has timed out! Use /deck edit");
        }
        Message msg;
        try {
            msg = client.getMessageById(Snowflake.of(deckEditing.getEditMsgChannelId()),
                    Snowflake.of(deckEditing.getEditMsgId())).onErrorStop().block();
        } catch (Exception e) {
            return ActionResult.fail("Could not find the original deck edit message! User /deck edit.");
        }

        if (msg == null) {
            return ActionResult.fail("Could not find the original deck edit message! User /deck edit.");
        }

        return ActionResult.success(Pair.of(deckEditing, msg));
    }

    public CardDeck saveDeck(CardDeck deck) {
        return deckRepository.save(deck);
    }
}