package net.stelitop.battledudestcg.discord.listeners.buttons;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.TextInput;
import discord4j.core.spec.InteractionPresentModalSpec;
import net.stelitop.battledudestcg.discord.ui.EditCardUI;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EditCardButtons implements ApplicationRunner {

    @Autowired
    private GatewayDiscordClient client;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private EditCardUI editCardUI;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        client.on(ButtonInteractionEvent.class, this::handle).subscribe();
    }

    /**
     * Handles all buttons related to editing a card. Their id should be of the format
     * "editcard|[card id]|[element to edit]|..."
     *
     * @param event The button event.
     * @return The mono.
     */
    private Mono<Void> handle(ButtonInteractionEvent event) {
        String componentId = event.getCustomId();
        if (!componentId.startsWith("editcard|")) return Mono.empty();
        String[] parts = componentId.split("\\|");
        long cardId = Long.parseLong(parts[1]);
        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isEmpty()) {
            return event.reply("An error occurred. Could not find a card with cardId = " + cardId + ".")
                    .withEphemeral(true);
        }
        Card card = cardOpt.get();
        return switch (parts[2]) { // find which button this is
            case EditCardUI.ID_NAME -> editName(event, card);
            case EditCardUI.ID_EFFECT -> editEffect(event, card);
            case EditCardUI.ID_ELEMENTAL_TYPES -> editTypes(event, card);
            default -> Mono.empty();
        };
    }

    /**
     * Handles the button about editing the name of the dude. The id should be in the
     * format "editcard|[card id]|{@value EditCardUI#ID_NAME}"
     */
    private Mono<Void> editName(ButtonInteractionEvent event, Card card) {

        return event.presentModal(InteractionPresentModalSpec.builder()
                .title("Edit " + card.getName() + "'s Name")
                .customId(editCardUI.makeId(card, EditCardUI.ID_NAME))
                .addComponent(ActionRow.of(TextInput.small("cardname", "New name for the card.", 1, 25)
                        .required(true)
                        .prefilled(card.getName())
                        .placeholder("New Name")))
                .build());
    }

    /**
     * Handles the button about editing the effect of the dude. The id should be in the
     * format "editcard|[card id]|{@value EditCardUI#ID_EFFECT}"
     */
    private Mono<Void> editEffect(ButtonInteractionEvent event, Card card) {
        return event.presentModal(InteractionPresentModalSpec.builder()
                .title("Edit " + card.getName() + "'s Effect Text")
                .customId(editCardUI.makeId(card, EditCardUI.ID_EFFECT))
                .addComponent(ActionRow.of(TextInput.paragraph("cardeffect", "New effect text for the card.", 0, 255)
                        .required(true)
                        .prefilled(card.getEffectText())
                        .placeholder("New Effect Text")))
                .build());
    }

    /**
     * Handles the button about editing the elemental types of the dude. The id should
     * be in the format "editcard|[card id]|{@value EditCardUI#ID_ELEMENTAL_TYPES}"
     */
    private Mono<Void> editTypes(ButtonInteractionEvent event, Card card) {
        return event.presentModal(InteractionPresentModalSpec.builder()
                .title("Edit " + card.getName() + "'s Types")
                .customId(editCardUI.makeId(card, EditCardUI.ID_ELEMENTAL_TYPES))
                .addComponent(ActionRow.of(TextInput.paragraph("cardeffect", "New elemental types for the card.", 1, 3)
                        .required(true)
                        .prefilled(card.getTypes().stream().map(x -> String.valueOf(x.toChar())).collect(Collectors.joining()))
                        .placeholder("New Elemental Types\n\nThey must be one of [EWAFNMTD*.]")))
                .build());
    }
}
