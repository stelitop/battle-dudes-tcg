package net.stelitop.battledudestcg.game.database.entities.collection;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.collection.UserCollection;

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
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "Card_In_Deck")
    private List<Card> cards = new ArrayList<>();

    /**
     * The name of the deck.
     */
    @Column(name = "name")
    private String name;

    /**
     * Gets the amount of copies in the collection of a specific card.
     *
     * @param name The name of the card.
     * @return How many copies are in the card.
     */
    public int countCopiesOfCard(String name) {
        if (cards == null) return 0;
        return (int)cards.stream()
                .filter(x -> x.getName().equalsIgnoreCase(name))
                .count();
    }
}
