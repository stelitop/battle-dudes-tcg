package net.stelitop.battledudestcg.game.enums;

import java.util.Map;

public enum Rarity {
    None,
    Basic,
    Common,
    Rare,
    Epic,
    Legendary,
    Mythic;

    /**
     * Downgrades a rarity to its previous tier.
     *
     * @return New rarity
     */
    public Rarity downgradeRarity() {
        return switch (this) {
            case Common, Rare -> Rarity.Common;
            case Epic -> Rarity.Rare;
            case Legendary -> Rarity.Epic;
            case Mythic -> Rarity.Legendary;
            default -> this;
        };
    }

    /**
     * Upgrades a rarity to its next tier.
     *
     * @return New rarity
     */
    public Rarity upgradeRarity() {
        return switch (this) {
            case None -> Rarity.Common;
            case Common -> Rarity.Rare;
            case Rare -> Rarity.Epic;
            case Epic -> Rarity.Legendary;
            case Legendary, Mythic -> Rarity.Mythic;
            default -> this;
        };
    }

    public int getCardLimit() {
        return switch (this) {
            case Basic -> 999;
            case Common -> 5;
            case Rare -> 4;
            case Epic -> 3;
            case Legendary -> 2;
            case Mythic -> 1;
            case None -> 0;
        };
    }

    public int getCoinValue() {
        return switch (this) {
            case Common -> 1;
            case Rare -> 4;
            case Epic -> 9;
            case Legendary -> 16;
            case Mythic -> 25;
            default -> 1;
        };
    }
}
