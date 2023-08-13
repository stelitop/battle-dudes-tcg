package net.stelitop.battledudestcg.game.database.entities.profile.collection.cards;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.UserCollection;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardOwnership {

    @EmbeddedId
    private UserCollectionCardKey id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("cardId")
    @JoinColumn(name = "card_id", referencedColumnName = "card_id")
    @ToString.Exclude
    private Card card;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("collectionId")
    @JoinColumn(name = "collection_id", referencedColumnName = "collection_id")
    @ToString.Exclude
    private UserCollection userCollection;

    @Column(name = "owned_copies")
    private int ownedCopies = 0;
}