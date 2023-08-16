package net.stelitop.battledudestcg.discord.ui;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import lombok.*;
import net.stelitop.battledudestcg.commons.pojos.ActionResult;
import net.stelitop.battledudestcg.discord.utils.ColorUtils;
import net.stelitop.battledudestcg.discord.utils.EmojiUtils;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.cards.DudeCard;
import net.stelitop.battledudestcg.game.database.entities.cards.ItemCard;
import net.stelitop.battledudestcg.game.database.entities.cards.WarpCard;
import net.stelitop.battledudestcg.game.database.entities.collection.CardOwnership;
import net.stelitop.battledudestcg.game.enums.ElementalType;
import net.stelitop.battledudestcg.game.enums.Rarity;
import net.stelitop.battledudestcg.game.services.UserProfileService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class CardCollectionUI {

    /**
     * How many different cards to be shown at once on a page in the card
     * collection ui. If there are more than this amount of cards, the
     * collection will be split into pages.
     */
    public static final int CARDS_PER_PAGE = 20;

    public static final String ID_CHANGE_COLLECTION_ORDERING = "changecollordering";

    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private EmojiUtils emojiUtils;
    @Autowired
    private ColorUtils colorUtils;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Model implements Cloneable {
        private static final String componentId = "changecollectionpage";
        private int page;
        /**
         * Type of the cards to show. Must be one of "all", "dude", "warp", "item"
         */
        private String cardType;
        private long userId;
        /**
         * How the cards to be ordered. Must be one of "default", "name", "eltypes" or "rarity"
         */
        private String ordering;

        public static @Nullable CardCollectionUI.Model deserialize(@NotNull String encoded) throws IllegalStateException{
            String[] parts = encoded.split("\\|");
            if (parts.length != 5 || !parts[0].equals(componentId)) return null;

            var model = Model.builder()
                    .userId(Long.parseLong(parts[1]))
                    .page(Integer.parseInt(parts[2]))
                    .cardType(parts[3])
                    .ordering(parts[4])
                    .build();
            ActionResult<Void> stateResult = model.validateState();
            return stateResult.isSuccessful() ? model : null;
        }

        public String serialize() throws IllegalStateException {
            if (validateState().hasFailed()) {
                throw new IllegalStateException(validateState().errorMessage());
            }
            return componentId + "|" + userId + "|" + page + "|" + cardType + "|" + ordering;
        }

        /**
         * Checks whether the values of the model are correct. If they are not an exception
         * is thrown.
         *
         * @return Success result if the state is valid, fail result otherwise
         *     with an error message.
         */
        public ActionResult<Void> validateState() throws IllegalStateException{
            if (!Set.of("all", "dude", "warp", "item").contains(cardType)) {
                return ActionResult.fail(cardType + " is not an allowed card type!");
            }
            if (!Set.of("default", "name", "eltypes", "rarity").contains(ordering)) {
                return ActionResult.fail(ordering + " is not an allowed ordering!");
            }
            return ActionResult.success();
        }

        @SneakyThrows
        public Model previousPage() {
            var x = (Model)this.clone();
            x.page--;
            return x;
        }
        @SneakyThrows
        public Model nextPage() {
            var x = (Model)this.clone();
            x.page++;
            return x;
        }
    }

    /**
     * Creates a UI message for a user's collection.
     *
     * @param model The model used for creating the collection message.
     * @param user The user that owns the message.
     * @return The UI message.
     */
    public MessageCreateSpec getCardCollectionMessage(Model model, User user) {
        var profile = userProfileService.getProfile(user.getId().asLong());

        List<CardOwnership> cardOwnerships = profile.getUserCollection().getOwnedCards().stream()
                .filter(x -> x.getOwnedCopies() > 0)
                .toList();
        int totalCards = cardOwnerships.stream()
                .filter(x -> x.getCard().getRarity() != Rarity.Basic)
                .mapToInt(CardOwnership::getOwnedCopies)
                .sum();
        int totalPages = 1 + (cardOwnerships.size() - 1)/ CARDS_PER_PAGE;
        model.page = Math.max(1, Math.min(totalPages, model.page));

        cardOwnerships = getRelevantCardOwnerships(cardOwnerships, model);
        String description = getDescription(cardOwnerships, totalCards, model.page, totalPages);

        List<Button> navigatePagesButtons = List.of(
                Button.primary(model.previousPage().serialize(), "Previous Page").disabled(model.page == 1),
                Button.primary(model.nextPage().serialize(), "Next Page").disabled(model.page == totalPages)
        );

        String changeOrderingMenuId = ID_CHANGE_COLLECTION_ORDERING + "|" + model.serialize();
        SelectMenu changeOrderingMenu = SelectMenu.of(changeOrderingMenuId, List.of(
                SelectMenu.Option.of("Arbitrary", "default"),
                SelectMenu.Option.of("Name", "name"),
                SelectMenu.Option.of("Elemental Types", "eltypes"),
                SelectMenu.Option.of("Rarity", "rarity")
        )).withPlaceholder("Set Ordering");

        String cardInfoSelectMenuId = "opencardinfopage|" + model.serialize();
        SelectMenu cardInfoSelectMenu = SelectMenu.of(cardInfoSelectMenuId, cardOwnerships.stream()
                        .map(x -> x.getCard().getName())
                        .map(x -> SelectMenu.Option.of(x, x))
                        .toList())
                .withPlaceholder("Get Card Info");

        return MessageCreateSpec.builder()
                .addEmbed(EmbedCreateSpec.builder()
                        .title(user.getUsername() + "'s Cards")
                        .description(description)
                        .color(colorUtils.getColor(ElementalType.Neutral))
                        .build())
                .addComponent(ActionRow.of(navigatePagesButtons))
                .addComponent(ActionRow.of(changeOrderingMenu))
                .addComponent(ActionRow.of(cardInfoSelectMenu))
                .build();
    }

    /**
     * Filters the list of all available card ownerships to only those that will be presented
     * in the card collection on the current page.
     *
     * @param allCardOwnerships List of all card ownerships.
     * @param model The model used for the ui.
     * @return A list of all card ownerships to be displayed.
     */
    private List<CardOwnership> getRelevantCardOwnerships(List<CardOwnership> allCardOwnerships, Model model) {

        return allCardOwnerships.stream()
                .filter(getTypeFilter(model.getCardType()))
                .sorted(getComparator(model.getOrdering()))
                .skip((long) CARDS_PER_PAGE * (model.page - 1))
                .limit(CARDS_PER_PAGE)
                .peek(x -> x.getCard().getTypes().addAll(
                        Collections.nCopies(Math.max(0, 3 - x.getCard().getTypes().size()), ElementalType.None)))
                .toList();
    }

    /**
     * Gets the description of the collection embed. It contains info about the cards to be
     * displayed, formatted to look as a table.
     *
     * @param cardOwnerships The card ownerships of the cards to be shown.
     * @param totalCards The total amount of cards in the collection.
     * @param currentPage The current page the user displays.
     * @param totalPages The total pages of the collection.
     * @return The description string to be used.
     */
    private String getDescription(
            List<CardOwnership> cardOwnerships,
            int totalCards,
            int currentPage,
            int totalPages
    ) {
        String description = cardOwnerships.stream()
                .map(this::formatCardOwnershipToString)
                .collect(Collectors.joining("\n"));

        description = "Total: " + totalCards + "\n\n"
                + ":1234: \u200B | \u200B :sparkles: \u200B | \u200B Ele Types \u200B \u200B | \u200B Name\n"
                + "================================\n"
                + description
                + "\n\nPage " + currentPage + "/" + totalPages;
        return description;
    }

    /**
     * Formats a card ownership object to be a row in a collection ui table.
     *
     * @param co The card ownership to format.
     * @return The formatted string.
     */
    private String formatCardOwnershipToString(CardOwnership co) {
        Card card = co.getCard();
        String cardAmountText = card.getRarity() != Rarity.Basic ?
                emojiUtils.getEmojiString(co.getOwnedCopies()) : ":hash:";

        return cardAmountText + " \u200B | \u200B "
                + emojiUtils.getEmojiString(card.getRarity()) + " \u200B | \u200B "
                + card.getTypes().stream()
                .map(emojiUtils::getEmojiString)
                .collect(Collectors.joining(" ")) + " \u200B | \u200B "
                + card.getName();
    }

    /**
     * Creates a comparator depending on the type of ordering wanted.
     *
     * @param ordering The type of ordering.
     * @return The comparator for card ownerships.
     */
    private Comparator<CardOwnership> getComparator(String ordering) {
        return switch (ordering) {
            case "name" -> (CardOwnership co1, CardOwnership co2) ->
                    co1.getCard().getName().compareToIgnoreCase(co2.getCard().getName());

            case "rarity" -> (CardOwnership co1, CardOwnership co2) ->
                    -co1.getCard().getRarity().compareTo(co2.getCard().getRarity());

            case "eltypes" -> (CardOwnership co1, CardOwnership co2) -> {
                Card c1 = co1.getCard(), c2 = co2.getCard();
                for (int i = 0; i < c1.getTypes().size() && i < c2.getTypes().size(); i++) {
                    if (c1.getTypes().get(i) != c2.getTypes().get(i)) {
                        return c1.getTypes().get(i).compareTo(c2.getTypes().get(i));
                    }
                }
                return Integer.compare(c1.getTypes().size(), c2.getTypes().size());
            };

            default -> (CardOwnership co1, CardOwnership co2) -> 0;
        };
    }

    /**
     * Creates a predicate that only accepts cards of a specific type.
     *
     * @param cardType The card type to accept.
     * @return The filter predicate.
     */
    private Predicate<CardOwnership> getTypeFilter(String cardType) {
        return co -> switch (cardType) {
            case "dude" -> co.getCard() instanceof DudeCard;
            case "item" -> co.getCard() instanceof ItemCard;
            case "warp" -> co.getCard() instanceof WarpCard;
            default -> true;
        };
    }
}
