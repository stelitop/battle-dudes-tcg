package net.stelitop.battledudestcg.game.database.entities.cards;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.enums.ElementalType;
import net.stelitop.battledudestcg.game.enums.Rarity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Card")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="card_type",
        discriminatorType = DiscriminatorType.STRING)

@Data
@SuperBuilder
@NoArgsConstructor
public class Card {

    /**
     * The unique id of the card.
     */
    @Id
    @Column(name = "card_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long cardId;

    /**
     * The name of the card.
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * The cost of the card. Every letter in the string is an element.
     */
    @Column(name = "cost", nullable = false)
    @Builder.Default
    private int cost = 0;

    /**
     * The text describing the effect of the card.
     */
    @Column(name = "effect_text", nullable = false)
    @Builder.Default
    private String effectText = "";

    /**
     * The elemental types of the card.
     */
    @Column(name = "types", nullable = false)
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(value = EnumType.STRING)
    @Singular
    private List<ElementalType> types;

    /**
     * The card's rarity.
     */
    @Column(name = "rarity", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private Rarity rarity = Rarity.None;

    /**
     * An url to an image containing the artwork of the card.
     *
     */
    @Column(name = "art_url", nullable = true)
    private String artUrl;

    /**
     * List of artists that worked on the artwork.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Singular
    private List<String> artists;

    /**
     * The flavor text of the card.
     */
    @Column(name = "flavor_text", nullable = true)
    private String flavorText;

    /**
     * The chests where this card can be obtained from.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @ToString.Include
    @Builder.Default
    private List<Chest> chestSources = new ArrayList<>();
}
