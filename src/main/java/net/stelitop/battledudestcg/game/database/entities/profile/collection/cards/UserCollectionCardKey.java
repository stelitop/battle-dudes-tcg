package net.stelitop.battledudestcg.game.database.entities.profile.collection.cards;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCollectionCardKey implements Serializable {

    @Column(name = "collection_id")
    private long collectionId;

    @Column(name = "card_id")
    private long cardId;

}
