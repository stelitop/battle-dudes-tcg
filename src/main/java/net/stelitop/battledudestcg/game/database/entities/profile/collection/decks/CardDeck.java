package net.stelitop.battledudestcg.game.database.entities.profile.collection.decks;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.UserCollection;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CardDeck")
@Data
public class CardDeck {

    /**
     * Unique id for the deck.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "deck_id")
    private long deckId;

    /**
     * The collection the deck belongs to.
     */
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "collection_id")
    @ToString.Exclude
    private UserCollection userCollection;

    /**
     * Cards part of the deck. Duplicates allowed.
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "Card_In_Deck")
    private List<Card> cards = new ArrayList<>();

    /**
     * The name of the deck.
     */
    @Column(name = "name")
    private String name;

}
