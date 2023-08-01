package net.stelitop.battledudestcg.game.enums;

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
     * @param rarity Rarity to downgrade.
     * @return New rarity
     */
    public Rarity downgradeRarity(Rarity rarity) {
        return switch (rarity) {
            case Rare -> Rarity.Common;
            case Epic -> Rarity.Rare;
            case Legendary -> Rarity.Epic;
            case Mythic -> Rarity.Legendary;
            default -> Rarity.None;
        };
    }

    /**
     * Upgrades a rarity to its next tier.
     *
     * @param rarity Rarity to upgrade.
     * @return New rarity
     */
    public Rarity upgradeRarity(Rarity rarity) {
        return switch (rarity) {
            case None -> Rarity.Common;
            case Common -> Rarity.Rare;
            case Rare -> Rarity.Epic;
            case Epic -> Rarity.Legendary;
            default -> Rarity.Mythic;
        };
    }
}
