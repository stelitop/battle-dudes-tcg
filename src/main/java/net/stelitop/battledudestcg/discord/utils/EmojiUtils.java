package net.stelitop.battledudestcg.discord.utils;

import net.stelitop.battledudestcg.game.enums.DudeStat;
import net.stelitop.battledudestcg.game.enums.ElementalType;
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
            case Air -> ":cloud:";
            case Fire -> ":fire:";
            case Earth -> ":bricks:";
            case Water -> ":bubbles:";
            case Tech -> ":nut_and_bolt:";
            case Magic -> ":crystal_ball:";
            case Decay -> ":bone:";
            case Nature -> ":herb:";
            case Neutral -> ":book:";
            default -> "";
        };
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
            default -> "";
        };
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
