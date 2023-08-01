package net.stelitop.battledudestcg.game.database.entities.profile.collection;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import net.stelitop.battledudestcg.game.database.entities.profile.UserProfile;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class UserCollection {

    @Id
    @Column(name = "collection_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long collectionId;

    @OneToOne(mappedBy = "userCollection")
    @ToString.Exclude
    private UserProfile userProfile;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "userCollection", cascade = CascadeType.ALL)
    private List<CardOwnership> ownedCards = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "userCollection", cascade = CascadeType.ALL)
    private List<ChestOwnership> ownedChests = new ArrayList<>();

    @Column(name = "coins")
    private int coins = 0;
}
