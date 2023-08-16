package net.stelitop.battledudestcg.game.database.entities.cards;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.stelitop.battledudestcg.game.enums.ElementalType;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@Entity
@DiscriminatorValue("Dude")

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DudeCard extends Card implements Comparable<DudeCard> {

    /**
     * The unique number of the dude.
     */
    @Column(name = "dude_id", unique = true)
    private long dudeId;

    /**
     * The stage of the dude
     */
    @Column()
    @IntRange(from = 1, to = 3)
    private int stage;

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
    @Column
    @NonNegative
    private int health;

    /**
     * The offense of the dude.
     */
    @Column
    @NonNegative
    private int offense;

    /**
     * The defence of the dude.
     */
    @Column
    @NonNegative
    private int defence;

    /**
     * The name of the dude this can evolve to. Null if there is no
     * next evolution.
     */
    @Column
    @ElementCollection(fetch = FetchType.EAGER)
    @Singular
    private List<String> nextEvolutions;

    /**
     * The name of the dude this must evolve from. Null if there is
     * no previous evolution.
     */
    @Column
    @ElementCollection(fetch = FetchType.EAGER)
    @Singular
    private List<String> previousEvolutions;

    /**
     * Gets the ID of the dude formatted as #abc
     *
     * @return Formatted ID.
     */
    public String getFormattedId() {
        long x = this.getDudeId();
        String ret = "";
        for (int i = 0; i < 3; i++) {
            ret = (x % 10) + ret;
            x /= 10;
        }
        return "#" + ret;
    }

    @Override
    public int compareTo(@NotNull DudeCard o) {
        if (this.dudeId == o.dudeId) return 0;
        return this.dudeId < o.dudeId ? -1 : 1;
    }
}
