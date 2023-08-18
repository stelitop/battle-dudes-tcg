package net.stelitop.battledudestcg.game.database.entities.collection;

import jakarta.persistence.*;
import lombok.*;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
    private int ownedCopies = 0;
}
