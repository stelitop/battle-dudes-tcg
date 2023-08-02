package net.stelitop.battledudestcg.discord.utils;

import discord4j.rest.util.Color;
import net.stelitop.battledudestcg.game.enums.ElementalType;
import net.stelitop.battledudestcg.game.enums.Rarity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ColorUtils {

    /**
     * Gets the colour of a given elemental type.
     *
     * @param elementalType The type.
     * @return The colour. If not found the default colour is black.
     */
    public Color getColor(ElementalType elementalType) {
        return switch (elementalType) {
            case Air -> Color.of(150, 217, 214); // #96d9d6
            case Fire -> Color.of(238, 129, 48); // #ee8130
            case Earth -> Color.of(226, 191, 101); // #e2bf65
            case Water -> Color.of(99, 144, 240); // #6390f0
            case Tech -> Color.of(183, 183, 206); // #b7b7ce
            case Magic -> Color.of(214, 133, 173); // #d685ad
            case Decay -> Color.of(112, 87, 70); // #705746
            case Nature -> Color.of(122, 199, 76); // #7ac74c
            case Neutral -> Color.of(168, 167, 122); // #a8a77a
            default -> Color.BLACK;
        };
    }

    /**
     * Mixes the colours of multiple elemntal types and produces one colour.
     *
     * @param elementalTypes The list of types the mix.
     * @return The mixed colour. If the list is empty, the neutral colour
     *         is returned.
     */
    public Color getColor(List<ElementalType> elementalTypes) {
        if (elementalTypes.isEmpty()) {
            return getColor(ElementalType.Neutral);
        }
        return Color.of(elementalTypes.stream()
                .map(this::getColor)
                .mapToInt(Color::getRGB)
                .sum() / elementalTypes.size());
    }

    /**
     * Gets the colour of a given rarity
     *
     * @param rarity The rarity to get the colour of.
     * @return The rarity's colour. If not found the default colour is black.
     */
    public Color getColor(Rarity rarity) {
        return switch (rarity) {
            case Common -> Color.of(66, 133, 244); // #4285f4
            case Rare -> Color.of(52, 168, 83); // #34a853
            case Epic -> Color.of(252, 205, 4); // #fbbc04
            case Legendary -> Color.of(255, 109, 1); // #ff6d01
            case Mythic -> Color.of(234, 67, 53); // #ea4335
            case Basic -> Color.WHITE;
            default -> Color.BLACK;
        };
    }

    /**
     * Gets the color that should be used for the chests on the side.
     *
     * @return The chest color.
     */
    public Color getChestEmbedColor() {
        return Color.of(253, 203, 88); // #FDCB58
    }
}
