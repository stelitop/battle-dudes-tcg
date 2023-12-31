package net.stelitop.battledudestcg.game.database.entities.chests;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.stelitop.battledudestcg.commons.utils.RandomUtils;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.enums.Rarity;
import net.stelitop.battledudestcg.game.pojo.ChestReward;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(name = "Chest")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="chest_type",
        discriminatorType = DiscriminatorType.STRING)
@Data
@SuperBuilder
@NoArgsConstructor
@ToString
public class Chest {

    /**
     * The default icon for a chest when there is no custom one present.
     */
    public static final String DEFAULT_CHEST_ICON_URL = "https://static.wikia.nocookie.net/minecraft_gamepedia/images/1/11/Chest_%28S%29_BE1.png/revision/latest?cb=20191203082210";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chest_id")
    @Builder.Default
    private long chestId = 0;

    /**
     * The name of the chest.
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * The description of the chest.
     */
    @Column(name = "description", nullable = false)
    @Builder.Default
    private String description = "(no description)";

    /**
     * How many items are found in the chest. The default for a chest is 10.
     */
    @Column(name = "items_count", nullable = false)
    @Builder.Default
    private int itemsCount = 10;

    /**
     * The cards that can be obtained from this chest.
     */
    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "chestSources")
    @ToString.Exclude
    @Singular
    private List<Card> possibleDrops;

    /**
     * The url for the icon of the chest.
     */
    @Column(name = "icon_url", nullable = true)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String iconUrl;

    public @NotNull ChestReward getRandomReward(@NotNull RandomUtils randomUtils) {
        if (this.getPossibleDrops().isEmpty()) {
            return ChestReward.coins(1);
        }
        // TODO: Move the location of this constant or generalise for the entity
        // try to get coins
        final double coinsProbability = 0.45;
        if (randomUtils.invokeProbability(coinsProbability)) {
            return ChestReward.coins(1);
        }

        Rarity rarity = randomUtils.pickWeightedRandomRarity();
        List<Card> possibleAwards = this.getPossibleDrops().stream()
                .filter(x -> x.getRarity().equals(rarity))
                .toList();
        Card pickedReward = randomUtils.getRandomItem(possibleAwards);
        if (pickedReward == null) {
            return ChestReward.coins(rarity.getCoinValue());
        }

        return ChestReward.card(pickedReward);
    }

    public @NotNull List<ChestReward> rollChest(@NotNull RandomUtils randomUtils) {
        List<ChestReward> rewards = new ArrayList<>();
        for (int i = 0; i < this.itemsCount; i++) {
            rewards.add(getRandomReward(randomUtils));
        }
        return rewards;
    }

    public @NotNull String getIconUrl() {
        return iconUrl == null ? DEFAULT_CHEST_ICON_URL : iconUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chest chest = (Chest) o;
        return chestId == chest.chestId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chestId, name, description, itemsCount, possibleDrops);
    }
}
