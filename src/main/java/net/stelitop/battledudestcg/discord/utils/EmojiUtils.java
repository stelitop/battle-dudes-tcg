package net.stelitop.battledudestcg.discord.utils;

import net.stelitop.battledudestcg.game.enums.DudeStat;
import net.stelitop.battledudestcg.game.enums.ElementalType;
import net.stelitop.battledudestcg.game.enums.Rarity;
import org.springframework.stereotype.Service;

@Service
public class EmojiUtils {

    /**
     * Transforms an elemental type into an emoji to draw.
     *
     * @param type The elemental type.
     * @return A string representing the emote.
     */
    public String getEmojiString(ElementalType type) {
        return switch (type) {
            case Air -> "<:airtype:1136113001376776225>";
            case Fire -> "<:firetype:1136113014911807539>";
            case Earth -> "<:earthtype:1136112965867810937>";
            case Water -> "<:watertype:1136112986457653328>";
            case Tech -> "<:techtype:1136113067663573053>";
            case Magic -> "<:magictype:1136113054279532675>";
            case Decay -> "<:decaytype:1136113041797300294>";
            case Nature -> "<:naturetype:1136113029386350672>";
            case Neutral -> "<:neutraltype:1136150480221905036>";
            case Ultimate -> "<:ultimatetype:1136120659714527242>";
            case None -> ":white_small_square:";
            default -> "";
        };
//        return switch (type) {
//            case Air -> ":cloud:";
//            case Fire -> ":fire:";
//            case Earth -> ":bricks:";
//            case Water -> ":bubbles:";
//            case Tech -> ":nut_and_bolt:";
//            case Magic -> ":crystal_ball:";
//            case Decay -> ":bone:";
//            case Nature -> ":herb:";
//            case Neutral -> ":book:";
//            case None -> ":white_small_square:";
//            default -> "";
//        };
    }

    /**
     * Gets the emoji string of one of a dude's stats.
     *
     * @param stat The stat.
     * @return The emoji string. Empty if it doesn't match.
     */
    public String getEmojiString(DudeStat stat) {
        return switch (stat) {
            case Health -> ":heart:";
            case Speed -> ":clock3:";
            case Offense -> ":crossed_swords:";
            case Defence -> ":shield:";
        };
    }

    /**
     * Gets the emoji string of one of a card's rarities.
     *
     * @param rarity The rarity.
     * @return The emoji string. Empty if it doesn't match.
     */
    public String getEmojiString(Rarity rarity) {
        return switch (rarity) {
            case Basic -> ":white_large_square:";
            case Common -> ":green_square:";
            case Rare -> ":blue_square:";
            case Epic -> ":purple_square:";
            case Legendary -> ":orange_square:";
            case Mythic -> ":red_square:";
            default -> "";
        };
    }

    /**
     * Gets the emoji string of a number. Every digit of the number
     * becomes its own emoji. Negative numbers are flipped into their
     * positive counterpart.
     *
     * @param number The number to change.
     * @return The emoji string of the number.
     */
    public String getEmojiString(int number) {
        if (number == 0) return ":zero:";
        if (number == 10) return ":keycap_ten:";
        if (number < 0) return getEmojiString(-number);
        StringBuilder ans = new StringBuilder();
        while (number > 0) {
            String letter = switch (number % 10) {
                case 0 -> ":zero:";
                case 1 -> ":one:";
                case 2 -> ":two:";
                case 3 -> ":three:";
                case 4 -> ":four:";
                case 5 -> ":five:";
                case 6 -> ":six:";
                case 7 -> ":seven:";
                case 8 -> ":eight:";
                case 9 -> ":nine:";
                default -> "";
            };
            ans.insert(0, letter);
            number /= 10;
        }
        return ans.toString();
    }

    /**
     * Gets the name of the emoji used for energy.
     *
     * @return The energy emoji.
     */
    public String getEnergyEmoji() {
        return ":zap:";
    }
}
