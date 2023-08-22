package net.stelitop.battledudestcg.discord.ui;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import lombok.*;
import net.stelitop.battledudestcg.discord.utils.ColorUtils;
import net.stelitop.battledudestcg.discord.utils.EmojiUtils;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.chests.ChannelChest;
import net.stelitop.battledudestcg.game.database.entities.chests.Chest;
import net.stelitop.battledudestcg.game.database.repositories.ChestRepository;
import net.stelitop.battledudestcg.game.enums.ElementalType;
import net.stelitop.battledudestcg.game.services.ChestService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChestInfoUI {

    /**
     * How many different cards to be shown at once on a page in the chest
     * info ui. If there are more than this amount of cards, the info page
     * will be split into pages.
     */
    public static final int CARDS_PER_PAGE = 20;

    @Autowired
    private ChestRepository chestRepository;
    @Autowired
    private ChestService chestService;
    @Autowired
    private ColorUtils colorUtils;
    @Autowired
    private EmojiUtils emojiUtils;

    /**
     * Model for serializing and deserializing the information about the UI to be
     * used for component ids.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Model implements Cloneable {
        public static final String COMPONENT_ID = "changechestinfopage";
        public static final String REGEX = COMPONENT_ID + "\\|[0-9]*\\|[0-9]*\\|[0-9]*";
        private int page;
        private long userId;
        private long chestId;
        public static @Nullable Model deserialize(@NotNull String encoded) throws IllegalStateException{
            String[] parts = encoded.split("\\|");
            if (parts.length != 4 || !parts[0].equals(COMPONENT_ID)) return null;

            return Model.builder()
                    .userId(Long.parseLong(parts[1]))
                    .chestId(Long.parseLong(parts[2]))
                    .page(Integer.parseInt(parts[3]))
                    .build();
        }

        public String serialize() {
            return COMPONENT_ID + "|" + userId + "|" + chestId + "|" + page;
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

    public MessageCreateSpec getChestInfoMessage(Model model) {
        Chest chest = chestRepository.findById(model.chestId).orElse(null);
        if (chest == null) {
            return MessageCreateSpec.builder()
                    .content("An error occurred finding chest with id = " + model.chestId + "!")
                    .build();
        }

        String description = "";
        if (chest instanceof ChannelChest c) {
            Optional<Long> channelId = chestService.getChannelIdOfChest(c);
            if (channelId.isPresent()) {
                description += "Channel: <#" + channelId.get() + ">\n\n";
            }
        }

        int totalCards = chest.getPossibleDrops().size();
        int totalPages = 1 + (totalCards - 1) / CARDS_PER_PAGE;
        model.page = Math.max(1, Math.min(totalPages, model.page));

        description += getPossibleCardDropsTable(chest.getPossibleDrops(), model.page, totalPages);
        description += "\n\n" + "*" + chest.getDescription() + "*";

        return MessageCreateSpec.builder()
                .addEmbed(EmbedCreateSpec.builder()
                        .title(chest.getName())
                        .description(description)
                        .thumbnail(chest.getIconUrl())
                        .color(colorUtils.getChestEmbedColor())
                        .build())
                .addComponent(ActionRow.of(
                        Button.primary(model.previousPage().serialize(), "Previous Page").disabled(model.page == 1),
                        Button.primary(model.nextPage().serialize(), "Next Page").disabled(model.page == totalPages)
                ))
                .build();
    }

    /**
     * Creates a formatted string in the form of a table to display information of the cards.
     * Only {@value CARDS_PER_PAGE} cards are taken from the given cards, depending on the
     * current page.
     *
     * @param cards The cards from which to create the field.
     * @param currentPage The number of the page being displayed
     * @param totalPages The total pages that the ui has.
     * @return The field for the embed.
     */
    private String getPossibleCardDropsTable(
            List<Card> cards,
            int currentPage,
            int totalPages
    ) {
        int totalCards = cards.size();
        String formattedCards = cards.stream()
                .sorted(Comparator.comparing(Card::getName))
                .sorted(Comparator.comparing(Card::getRarity).reversed())
                .skip((long) CARDS_PER_PAGE * (currentPage - 1))
                .limit(CARDS_PER_PAGE)
                .map(this::formatCardToString)
                .collect(Collectors.joining("\n"));

        String eleTypesHeader = emojiUtils.getEmojiString(ElementalType.None);
        eleTypesHeader = eleTypesHeader + " " + eleTypesHeader + " " + eleTypesHeader;
        return "Possible Drops: " + totalCards + "\n\n"
                + ":sparkles: \u200B | \u200B " + eleTypesHeader + " \u200B | \u200B Name\n"
                + "=========================\n"
                + formattedCards
                + "\n\nPage " + currentPage + "/" + totalPages;
    }

    /**
     * Formats a card to be a row in a chest info ui table.
     *
     * @param card The card to format.
     * @return The formatted string.
     */
    private String formatCardToString(Card card) {
        var types = card.getTypes();
        types.addAll(Collections.nCopies(3 - types.size(), ElementalType.None));
        return emojiUtils.getEmojiString(card.getRarity()) + " \u200B | \u200B "
                + types.stream().map(emojiUtils::getEmojiString)
                    .collect(Collectors.joining(" ")) + " \u200B | \u200B "
                + card.getName();
    }
}
