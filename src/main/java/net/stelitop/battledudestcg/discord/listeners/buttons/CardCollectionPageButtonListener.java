package net.stelitop.battledudestcg.discord.listeners.buttons;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import lombok.*;
import net.stelitop.battledudestcg.commons.utils.RandomUtils;
import net.stelitop.battledudestcg.discord.utils.ColorUtils;
import net.stelitop.battledudestcg.discord.utils.EmojiUtils;
import net.stelitop.battledudestcg.game.database.entities.cards.DudeCard;
import net.stelitop.battledudestcg.game.database.entities.cards.ItemCard;
import net.stelitop.battledudestcg.game.database.entities.cards.WarpCard;
import net.stelitop.battledudestcg.game.database.entities.profile.collection.CardOwnership;
import net.stelitop.battledudestcg.game.database.repositories.ChestOwnershipRepository;
import net.stelitop.battledudestcg.game.database.repositories.ChestRepository;
import net.stelitop.battledudestcg.game.enums.ElementalType;
import net.stelitop.battledudestcg.game.services.CollectionService;
import net.stelitop.battledudestcg.game.services.UserProfileService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CardCollectionPageButtonListener implements ApplicationRunner {

    public static final int CARDS_PER_PAGE = 20;

    @Autowired
    private GatewayDiscordClient client;
    @Autowired
    private ChestRepository chestRepository;
    @Autowired
    private RandomUtils randomUtils;
    @Autowired
    private CollectionService collectionService;
    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private ChestOwnershipRepository chestOwnershipRepository;
    @Autowired
    private EmojiUtils emojiUtils;
    @Autowired
    private ColorUtils colorUtils;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        client.on(ButtonInteractionEvent.class, this::handle).subscribe();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CollectionUiModel implements Cloneable {
        private static final String componentId = "changecollectionpage";
        private int page;
        /**
         * Type of the cards to show. Must be one of "all", "dude", "warp", "item"
         */
        private String cardType;
        private long userId;

        public static @Nullable CollectionUiModel deserialize(@NotNull String encoded) {
            String[] parts = encoded.split("\\|");
            if (parts.length != 4 || !parts[0].equals(componentId)) return null;
            try {
                return CollectionUiModel.builder()
                        .userId(Long.parseLong(parts[1]))
                        .page(Integer.parseInt(parts[2]))
                        .cardType(parts[3])
                        .build();
            } catch (Exception ignore) {
                return null;
            }
        }

        public String serialize() {
            return componentId + "|" + userId + "|" + page + "|" + cardType;
        }

        @SneakyThrows
        public CollectionUiModel previousPage() {
            var x = (CollectionUiModel)this.clone();
            x.page--;
            return x;
        }
        @SneakyThrows
        public CollectionUiModel nextPage() {
            var x = (CollectionUiModel)this.clone();
            x.page++;
            return x;
        }
    }

    private Mono<Void> handle(ButtonInteractionEvent event) {
        String buttonId = event.getCustomId();
        CollectionUiModel model = CollectionUiModel.deserialize(buttonId);
        if (model == null) {
            return Mono.empty();
        }
        if (model.userId != event.getInteraction().getUser().getId().asLong()) {
            return event.reply()
                    .withContent("This is not your collection message!")
                    .withEphemeral(true);
        }
        MessageCreateSpec message = getCardCollectionMessage(model, event.getInteraction().getUser());
        return event.edit()
                .withContent(message.content())
                .withEmbeds(message.embeds())
                .withComponents(message.components());
    }

    /**
     * TODO: Move this method somewhere else.
     *
     * @param model
     * @return
     */
    public MessageCreateSpec getCardCollectionMessage(CollectionUiModel model, User user) {
        String username = user.getUsername();
        var profile = userProfileService.getProfile(user.getId().asLong());

        List<CardOwnership> cardOwnerships = profile.getUserCollection().getOwnedCards().stream()
                .filter(x -> x.getOwnedCopies() > 0)
                .toList();

        int totalCards = cardOwnerships.stream()
                .mapToInt(CardOwnership::getOwnedCopies)
                .sum();

        // filter depending on the requested type
        cardOwnerships = cardOwnerships.stream()
                .filter(co -> switch (model.cardType) {
                    case "dude" -> co.getCard() instanceof DudeCard;
                    case "item" -> co.getCard() instanceof ItemCard;
                    case "warp" -> co.getCard() instanceof WarpCard;
                    default -> true;
                }).toList();

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

        return MessageCreateSpec.builder()
                .addEmbed(EmbedCreateSpec.builder()
                        .title(username + "'s Cards")
                        .description(description)
                        .color(colorUtils.getColor(ElementalType.Neutral))
                        .build())
                .addComponent(ActionRow.of(navigatePagesButtons))
                .build();
    }
}
