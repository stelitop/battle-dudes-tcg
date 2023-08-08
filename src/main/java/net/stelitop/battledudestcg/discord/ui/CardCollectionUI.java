package net.stelitop.battledudestcg.discord.ui;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import lombok.*;
import net.stelitop.battledudestcg.discord.utils.ColorUtils;
import net.stelitop.battledudestcg.discord.utils.EmojiUtils;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.cards.DudeCard;
import net.stelitop.battledudestcg.game.database.entities.cards.ItemCard;
import net.stelitop.battledudestcg.game.database.entities.cards.WarpCard;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.CardOwnership;
import net.stelitop.battledudestcg.game.enums.ElementalType;
import net.stelitop.battledudestcg.game.services.UserProfileService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CardCollectionUI {

    public static final int CARDS_PER_PAGE = 20;

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
            try {
                var model = Model.builder()
                        .userId(Long.parseLong(parts[1]))
                        .page(Integer.parseInt(parts[2]))
                        .cardType(parts[3])
                        .ordering(parts[4])
                        .build();
                model.validateState();
                return model;
            } catch (Exception ignore) {
                return null;
            }
        }

        public String serialize() throws IllegalStateException{
            validateState();
            return componentId + "|" + userId + "|" + page + "|" + cardType + "|" + ordering;
        }

        /**
         * Checks whether the values of the model are correct. If they are not an exception
         * is thrown.
         *
         * @throws IllegalStateException
         */
        private void validateState() throws IllegalStateException{
            if (!Set.of("all", "dude", "warp", "item").contains(cardType)) {
                throw new IllegalStateException(cardType + " is not an allowed card type!");
            }
            if (!Set.of("default", "name", "eltypes", "rarity").contains(ordering)) {
                throw new IllegalStateException(ordering + " is not an allowed ordering!");
            }
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
     * TODO: Move this method somewhere else.
     *
     * @param model
     * @return
     */
    public MessageCreateSpec getCardCollectionMessage(Model model, User user) {
        String username = user.getUsername();
        var profile = userProfileService.getProfile(user.getId().asLong());

        List<CardOwnership> cardOwnerships = profile.getUserCollection().getOwnedCards().stream()
                .filter(x -> x.getOwnedCopies() > 0)
                .toList();

        // filter depending on the requested type
        cardOwnerships = cardOwnerships.stream()
                .filter(co -> switch (model.cardType) {
                    case "dude" -> co.getCard() instanceof DudeCard;
                    case "item" -> co.getCard() instanceof ItemCard;
                    case "warp" -> co.getCard() instanceof WarpCard;
                    default -> true;
                }).toList();

        // ordering
        cardOwnerships = new ArrayList<>(cardOwnerships);
        orderCards(cardOwnerships, model.getOrdering());

        int totalCards = cardOwnerships.stream()
                .mapToInt(CardOwnership::getOwnedCopies)
                .sum();

        int totalPages = 1 + (cardOwnerships.size() - 1)/ CARDS_PER_PAGE;
        model.page = Math.max(1, Math.min(totalPages, model.page));

        cardOwnerships = cardOwnerships.subList(
                CARDS_PER_PAGE * (model.page - 1),
                Math.min(CARDS_PER_PAGE * model.page, cardOwnerships.size())
        );

        // fill with blank types to align for the ui
        cardOwnerships.forEach(x -> x.getCard().getTypes().addAll(
                Collections.nCopies(Math.max(0, 3 - x.getCard().getTypes().size()), ElementalType.None)));

        String description = cardOwnerships.stream()
                .map(co -> emojiUtils.getEmojiString(co.getOwnedCopies()) + " \u200B | \u200B "
                        + emojiUtils.getEmojiString(co.getCard().getRarity()) + " \u200B | \u200B "
                        + co.getCard().getTypes().stream()
                        .map(emojiUtils::getEmojiString)
                        .collect(Collectors.joining(" ")) + " \u200B | \u200B "
                        + co.getCard().getName())
                .collect(Collectors.joining("\n"));

        description = "Total: " + totalCards + "\n\n"
                + ":hash: \u200B | \u200B :sparkles: \u200B | \u200B Ele Types \u200B \u200B | \u200B Name\n"
                + "================================\n"
                + description
                + "\n\nPage " + model.page + "/" + totalPages;

        List<Button> navigatePagesButtons = new ArrayList<>();
        navigatePagesButtons.add(Button.primary(model.previousPage().serialize(), "Previous Page")
                .disabled(model.page == 1));
        navigatePagesButtons.add(Button.primary(model.nextPage().serialize(), "Next Page")
                .disabled(model.page == totalPages));

        String selectMenuId = "opencardinfopage|" + model.serialize();
        SelectMenu selectMenu = SelectMenu.of(selectMenuId, cardOwnerships.stream()
                        .map(x -> x.getCard().getName())
                        .map(x -> SelectMenu.Option.of(x, x))
                        .toList())
                .withPlaceholder("Card Info");


        return MessageCreateSpec.builder()
                .addEmbed(EmbedCreateSpec.builder()
                        .title(username + "'s Cards")
                        .description(description)
                        .color(colorUtils.getColor(ElementalType.Neutral))
                        .build())
                .addComponent(ActionRow.of(navigatePagesButtons))
                .addComponent(ActionRow.of(selectMenu))
                .build();
    }

    private List<CardOwnership> getRelevantCardOwnerships(List<CardOwnership> allCardOwnerships) {
        return allCardOwnerships;
    }

    private void orderCards(List<CardOwnership> cardOwnerships, String ordering) {
        switch (ordering) {
            case "name":
                cardOwnerships.sort(Comparator.comparing(x -> x.getCard().getName().toLowerCase()));
                break;
            case "eltypes":
                cardOwnerships.sort((o1, o2) -> {
                    Card c1 = o1.getCard(), c2 = o2.getCard();
                    for (int i = 0; i < c1.getTypes().size() && i < c2.getTypes().size(); i++) {
                        if (c1.getTypes().get(i) != c2.getTypes().get(i)) {
                            return c1.getTypes().get(i).compareTo(c2.getTypes().get(i));
                        }
                    }
                    return Integer.compare(c1.getTypes().size(), c2.getTypes().size());
                });
                break;
            case "rarity":
                cardOwnerships.sort(Comparator.comparing(x -> ((CardOwnership)x).getCard().getRarity()).reversed());
                break;
        }
    }
}
