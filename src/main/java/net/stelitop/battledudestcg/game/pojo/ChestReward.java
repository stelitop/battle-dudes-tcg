package net.stelitop.battledudestcg.game.pojo;

import lombok.Getter;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.enums.Rarity;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Getter
public class ChestReward {

    private RewardType rewardType = RewardType.None;
    private int coins = -1;
    private Card card = null;

    private ChestReward() {

    }

    public static ChestReward coins(@Positive int amount) {
        ChestReward reward = new ChestReward();
        reward.coins = amount;
        reward.rewardType = RewardType.Coins;
        return reward;
    }

    public static ChestReward card(@NotNull Card card) {
        ChestReward reward = new ChestReward();
        reward.card = card;
        reward.rewardType = RewardType.Card;
        return reward;
    }

    public static ChestReward extraCard(@NotNull Card card, @Positive int pityAmount) {
        ChestReward reward = new ChestReward();
        reward.card = card;
        reward.coins = pityAmount;
        reward.rewardType = RewardType.ExtraCard;
        return reward;
    }

    @Override
    public String toString() {
        return switch (rewardType) {
            case Coins -> toStringCoinsv2();
            case Card -> toStringCardv2();
            case ExtraCard -> toStringExtraCardv2();
            case None -> "(Empty Reward)";
        };
    }

    private String toStringCoinsv1() {
        if (coins == 1) {
            return ":coin:";
        } else {
            return ":coin: x" + coins;
        }
    }

    private String toStringCardv1() {
        return switch (card.getRarity()) {
            case Common -> card.getName();
            case Rare -> ":small_blue_diamond: " + card.getName() + " :small_blue_diamond:";
            case Epic -> ":purple_circle: " + card.getName() + " :purple_circle:";
            case Legendary -> ":large_orange_diamond: **" + card.getName() + "** :large_orange_diamond:";
            case Mythic -> ":sparkles::sparkles: __**" + card.getName() + "**__ :sparkles::sparkles:";
            default -> card.getName();
        };
    }

    private String toStringCoinsv2() {
        return ":coin: \u200B " + coins + " Coin" + (coins != 1 ? "s" : "");
    }

    private String toStringCardv2() {
        return switch (card.getRarity()) {
            case Common -> ":green_square: \u200B " + card.getName();
            case Rare -> ":blue_square: \u200B *" + card.getName() + "*";
            case Epic -> ":purple_square: \u200B ***" + card.getName() + "***";
            case Legendary -> ":orange_square: \u200B __***" + card.getName() + "***__";
            case Mythic -> ":red_square: \u200B __***" + card.getName().toUpperCase() + "***__";
            default -> card.getName();
        };
    }

    private String toStringExtraCardv2() {
        return switch (card.getRarity()) {
            case Common -> ":green_square: \u200B ~~ \u200B " + card.getName() + " ~~";
            case Rare -> ":blue_square: \u200B *~~ \u200B " + card.getName() + " ~~*";
            case Epic -> ":purple_square: \u200B ***~~ \u200B " + card.getName() + " ~~***";
            case Legendary -> ":orange_square: \u200B ***~~ \u200B __" + card.getName() + "__ ~~***";
            case Mythic -> ":red_square: \u200B ***~~ \u200B __" + card.getName().toUpperCase() + "__ ~~***";
            default -> card.getName();
        } + "\u200B \u200B -> \u200B " + toStringCoinsv2();
    }

    public enum RewardType {
        None,
        Coins,
        Card,
        ExtraCard
    }
}
