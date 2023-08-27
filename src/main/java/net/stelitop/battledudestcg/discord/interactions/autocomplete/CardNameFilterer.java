package net.stelitop.battledudestcg.discord.interactions.autocomplete;

import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.cards.DudeCard;
import net.stelitop.battledudestcg.game.enums.ElementalType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
public class CardNameFilterer {

    /**
     * <p>Filters a list of cards by applying a filter, obtained from a specially
     * formatted string.</p>
     *
     * The filter contains space
     *
     * @param filter The string filter.
     * @param cards The cards to filter.
     * @return The list of cards filtered.
     */
    public List<Card> applySpecialFilter(String filter, List<Card> cards) {
        Map<String, String> filters = extractFilters(filter);
        List<Predicate<Card>> conditions = new ArrayList<>();
        for (String key : filters.keySet()) {
            final String value = filters.get(key);
            Predicate<Card> newPred = switch (key) {
                case "name" -> c -> c.getName().toLowerCase().contains(value);
                case "rarity" -> c -> c.getRarity().name().equalsIgnoreCase(value);
                case "types" -> getTypesFilter(value);
                case "health", "hp" -> c -> c instanceof DudeCard d && getNumberFilter(value, c2 -> ((DudeCard) c2).getHealth()).test(d);
                case "offence", "offense", "off" -> c -> c instanceof DudeCard d && getNumberFilter(value, c2 -> ((DudeCard) c2).getOffense()).test(d);
                case "defence", "defense", "def" -> c -> c instanceof DudeCard d && getNumberFilter(value, c2 -> ((DudeCard) c2).getDefence()).test(d);
                default -> null;
            };
            if (newPred != null) conditions.add(newPred);
        }
        List<Card> ret = new ArrayList<>();
        for (var card : cards) {
            boolean allowed = true;
            for (var pred : conditions) {
                if (!pred.test(card)) {
                    allowed = false;
                    break;
                }
            }
            if (allowed) ret.add(card);
        }
        return ret;
    }

    /**
     * Extracts all filters present in a special filter string.
     *
     * @param filter The filter string to parse.
     * @return A map that maps the found keys to the inputted values. All keys
     *     and values are guaranteed to be lowercase strings.
     */
    private Map<String, String> extractFilters(String filter) {
        Map<String, String> ret = new HashMap<>();
        String[] segments = filter.split(" ");
        StringBuilder cardName = new StringBuilder();
        for (String segment : segments) {
            if (!segment.contains(":")) {
                cardName.append(segment).append(" ");
                continue;
            }
            String[] queryParts = segment.split(":");
            if (queryParts.length != 2) continue;
            ret.put(queryParts[0].toLowerCase(), queryParts[1].toLowerCase());
        }
        ret.put("name", cardName.toString().trim());
        return ret;
    }

    private Predicate<Card> getTypesFilter(String value) {
        List<ElementalType> types = ElementalType.parseString(value);
        if (types == null) return c -> false;
        return c -> new HashSet<>(c.getTypes()).containsAll(types);
    }

    private Predicate<Card> getNumberFilter(String value, Function<? super Card, Integer> valueExtractor) {
        char sign = '=';
        if (value.startsWith(">")) {
            sign = '>';
            value = value.substring(1);
        } else if (value.startsWith("<")) {
            sign = '<';
            value = value.substring(1);
        }
        int numValue;
        try {
            numValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return c -> false;
        }

        return switch (sign) {
            case '=' -> c -> valueExtractor.apply(c) == numValue;
            case '>' -> c -> valueExtractor.apply(c) > numValue;
            case '<' -> c -> valueExtractor.apply(c) < numValue;
            default -> c -> false;
        };
    }
}
