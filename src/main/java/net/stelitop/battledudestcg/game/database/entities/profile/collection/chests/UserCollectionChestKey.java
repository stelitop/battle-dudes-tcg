package net.stelitop.battledudestcg.game.database.entities.profile.collection.chests;

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
public class UserCollectionChestKey implements Serializable {

    @Column(name = "collection_id")
    private long collectionId;

    @Column(name = "chest_id")
    private long chestId;

}
