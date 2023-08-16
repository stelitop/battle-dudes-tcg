package net.stelitop.battledudestcg.game.enums;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum ElementalType {
    Neutral,
    Earth,
    Water,
    Air,
    Fire,
    Nature,
    Decay,
    Magic,
    Tech,
    None,
    Ultimate;

    public char toChar() {
        return switch (this) {
            case Air -> 'A';
            case Fire -> 'F';
            case Earth -> 'E';
            case Water -> 'W';
            case Tech -> 'T';
            case Decay -> 'D';
            case Magic -> 'M';
            case Nature -> 'N';
            case Neutral -> '.';
            case Ultimate -> '*';
            case None -> ' ';
        };
    }

    public static ElementalType fromChar(char c) {
        if ('a' <= c && c <= 'z') c = (char)(c - 'a' + 'A');
        return switch (c) {
            case 'A' -> Air;
            case 'F' -> Fire;
            case 'E' -> Earth;
            case 'W' -> Water;
            case 'T' -> Tech;
            case 'D' -> Decay;
            case 'M' -> Magic;
            case 'N' -> Nature;
            case '.' -> Neutral;
            case '*' -> Ultimate;
            default -> None;
        };
    }

    /**
     * Parses a string that contains elemental types for each character. The string
     * is parsed using {@link ElementalType#fromChar(char)} on every character. If
     * there is an unknown character, null is returned instead.
     *
     * @param types The string containing the types. If null is passed, an empty list
     *     is returned instead.
     * @return The list of the parsed elemental types, or null if there are any invalid
     *     ones. The list is guaranteed to be mutable.
     */
    public static @Nullable List<ElementalType> parseString(@Nullable String types) {
        if (types == null) return new ArrayList<>();
        List<ElementalType> ret = new ArrayList<>();
        for (int i = 0; i < types.length(); i++) {
            ElementalType type = fromChar(types.charAt(i));
            if (type == None) return null;
            ret.add(type);
        }
        return ret;
    }
}
