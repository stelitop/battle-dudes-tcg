package net.stelitop.battledudestcg.game.database.entities.profile.collection;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import net.stelitop.battledudestcg.game.database.entities.profile.UserProfile;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.cards.CardOwnership;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.chests.ChestOwnership;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.decks.CardDeck;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.decks.DeckEditing;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "UserCollection")
@Data
public class UserCollection {

    /**
     * The unique id of the collection.
     */
    @Id
    @Column(name = "collection_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long collectionId;

    /**
     * The user profile this collection belongs to.
     */
    @OneToOne(mappedBy = "userCollection", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @ToString.Exclude
    private UserProfile userProfile;

    /**
     * The cards that are part of this collection.
     */
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "userCollection", cascade = CascadeType.ALL)
    private List<CardOwnership> ownedCards = new ArrayList<>();

    /**
     * The chests that are part of this collection.
     */
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "userCollection", cascade = CascadeType.ALL)
    private List<ChestOwnership> ownedChests = new ArrayList<>();

    /**
     * The decks that are part of this collection.
     */
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "userCollection", cascade = CascadeType.ALL)
    private List<CardDeck> decks = new ArrayList<>();

    /**
     * The data related to users editing their deck.
     */
    @Embedded
    private DeckEditing deckEditing = new DeckEditing();

    /**
     * How many coins are in the collection
     */
    @Column(name = "coins")
    private int coins = 0;
}
