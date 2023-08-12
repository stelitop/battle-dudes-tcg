package net.stelitop.battledudestcg.game.database.entities.profile.collection.chests;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.UserCollection;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.chests.UserCollectionChestKey;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChestOwnership {

    @EmbeddedId
    private UserCollectionChestKey id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @MapsId("chestId")
    @JoinColumn(name = "chest_id", referencedColumnName = "chest_id")
    @ToString.Exclude
    private Chest chest;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @MapsId("collectionId")
    @JoinColumn(name = "collection_id", referencedColumnName = "collection_id")
    @ToString.Exclude
    private UserCollection userCollection;

    @Column(name = "count")
    private int count = 0;
}
