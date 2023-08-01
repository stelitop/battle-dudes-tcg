package net.stelitop.battledudestcg.game;

import jakarta.persistence.*;
import lombok.*;
import net.stelitop.battledudestcg.game.enums.ElementalType;
import net.stelitop.battledudestcg.game.enums.Rarity;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ForeignDude implements Comparable<ForeignDude> {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * The name of the dude,
     */
    @Column(nullable = false, unique = true, length = 30)
    private String name;

    /**
     * The stage of the dude
     */
    @Column(nullable = false)
    @IntRange(from = 1, to = 3)
    private int stage;

    /**
     * The name of the dude this can evolve to. Null if there is no
     * next evolution.
     */
    @Column(nullable = true)
    @ElementCollection(fetch = FetchType.EAGER)
    @Singular
    private List<String> nextEvolutions;

    /**
     * The name of the dude this must evolve from. Null if there is
     * no previous evolution.
     */
    @Column(nullable = true)
    @ElementCollection(fetch = FetchType.EAGER)
    @Singular
    private List<String> previousEvolutions;

    /**
     * The types of the dude.
     */
    @Column
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(value = EnumType.STRING)
    @Singular
    private List<ElementalType> types;

    /**
     * The type this dude is resistant to. Null if none.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(value = EnumType.STRING)
    @Singular
    private List<ElementalType> resistances;

    /**
     * The type this dude is weak to. Null if none.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(value = EnumType.STRING)
    @Singular
    private List<ElementalType> weaknesses;

    /**
     * The health of the dude.
     */
    @Column(nullable = false)
    @NonNegative
    private int health;

    /**
     * The speed of the dude.
     */
    @Column(nullable = false)
    @NonNegative
    private int speed;

    /**
     * The offense of the dude.
     */
    @Column(nullable = false)
    @NonNegative
    private int offense;

    /**
     * The defense of the dude.
     */
    @Column(nullable = false)
    @NonNegative
    private int defense;

    /**
     * The name of the artist.
     */
    @Column(nullable = false, length = 30)
    private String artistName;

    /**
     * A url link to the artwork of the dude.
     */
    @Column(nullable = false)
    private String artLink;

    /**
     * The rarity of the dude. This bases the chances of getting them while
     * chatting.
     */
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Rarity rarity;

    /**
     * The ids of the discord channels you can acquire this dude in. Empty
     * means it can appear in any.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Singular
    //@Getter(AccessLevel.NONE)
    private List<Long> locations;

//    public List<Long> getLocations() {
//        Hibernate.initialize(this.locations);
//        return this.locations;
//    }

    /**
     * The flavor text of the dude. Has no impact on gameplay.
     */
    @Column
    private String flavorText;

    @Override
    public int compareTo(@NotNull ForeignDude o) {
        if (this.id == o.getId()) return 0;
        return this.id < o.getId() ? -1 : 1;
    }
}
