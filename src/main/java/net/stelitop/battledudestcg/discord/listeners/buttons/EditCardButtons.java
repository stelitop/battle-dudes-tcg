package net.stelitop.battledudestcg.discord.listeners.buttons;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.TextInput;
import discord4j.core.spec.InteractionPresentModalSpec;
import net.stelitop.battledudestcg.discord.framework.components.ComponentInteraction;
import net.stelitop.battledudestcg.discord.framework.DiscordEventsComponent;
import net.stelitop.battledudestcg.discord.framework.InteractionEvent;
import net.stelitop.battledudestcg.discord.ui.EditCardUI;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.cards.DudeCard;
import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.stream.Collectors;

@DiscordEventsComponent
public class EditCardButtons {

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private EditCardUI editCardUI;

    /**
     * Handles all buttons related to editing a card. Their id should be of the format
     * "editcard|[card id]|[element to edit]|..."
     *
     * @param event The button event.
     * @return The event reply.
     */
    @ComponentInteraction(event = ButtonInteractionEvent.class, regex = "editcard\\|[0-9]*\\|[a-zA-Z]*.*")
    public Mono<Void> editButtonPressedInteraction(@InteractionEvent ButtonInteractionEvent event) {
        String componentId = event.getCustomId();
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
            case EditCardUI.ID_STATS -> (card instanceof DudeCard d) ? editStats(event, d) : Mono.empty();
            case EditCardUI.ID_COST -> editCost(event, card);
            case EditCardUI.ID_ART_URL -> editArtUrl(event, card);
            case EditCardUI.ID_RARITY -> editRarity(event, card);
            default -> Mono.empty();
        };
    }

    /**
     * Handles the button about editing the name of the card. The id should be in the
     * format "editcard|[card id]|{@value EditCardUI#ID_NAME}"
     */
    private Mono<Void> editName(ButtonInteractionEvent event, Card card) {

        return event.presentModal(InteractionPresentModalSpec.builder()
                .title("Edit " + card.getName() + "'s Name")
                .customId(editCardUI.makeId(card, EditCardUI.ID_NAME))
                .addComponent(ActionRow.of(TextInput.small("cardname", "Name", 1, 25)
                        .required(true)
                        .prefilled(card.getName())
                        .placeholder("New Name")))
                .build());
    }

    /**
     * Handles the button about editing the effect of the card. The id should be in the
     * format "editcard|[card id]|{@value EditCardUI#ID_EFFECT}"
     */
    private Mono<Void> editEffect(ButtonInteractionEvent event, Card card) {
        return event.presentModal(InteractionPresentModalSpec.builder()
                .title("Edit " + card.getName() + "'s Effect Text")
                .customId(editCardUI.makeId(card, EditCardUI.ID_EFFECT))
                .addComponent(ActionRow.of(TextInput.paragraph("cardeffect", "Effect Text", 0, 255)
                        .required(true)
                        .prefilled(card.getEffectText())
                        .placeholder("New Effect Text")))
                .build());
    }

    /**
     * Handles the button about editing the elemental types of the card. The id should
     * be in the format "editcard|[card id]|{@value EditCardUI#ID_ELEMENTAL_TYPES}"
     */
    private Mono<Void> editTypes(ButtonInteractionEvent event, Card card) {
        return event.presentModal(InteractionPresentModalSpec.builder()
                .title("Edit " + card.getName() + "'s Types")
                .customId(editCardUI.makeId(card, EditCardUI.ID_ELEMENTAL_TYPES))
                .addComponent(ActionRow.of(TextInput.paragraph("cardtypes", "Elemental Type [EWAFNMTD*.]", 0, 3)
                        .required(true)
                        .prefilled(card.getTypes().stream().map(x -> String.valueOf(x.toChar())).collect(Collectors.joining()))
                        .placeholder("New Elemental Types\n\nThey must be one of [EWAFNMTD*.]")))
                .build());
    }

    /**
     * Handles the button about editing the stats of the dude. The id should be in the
     * format "editcard|[card id]|{@value EditCardUI#ID_STATS}"
     */
    private Mono<Void> editStats(ButtonInteractionEvent event, DudeCard card) {
        return event.presentModal(InteractionPresentModalSpec.builder()
                .title("Edit " + card.getName() + "'s Stats")
                .customId(editCardUI.makeId(card, EditCardUI.ID_STATS))
                .addComponent(ActionRow.of(TextInput.small("cardhealth", "Health", 1, 5)
                        .required(true)
                        .prefilled(String.valueOf(card.getHealth()))
                        .placeholder("New Health")))
                .addComponent(ActionRow.of(TextInput.small("cardoffense", "Offense", 1, 5)
                        .required(true)
                        .prefilled(String.valueOf(card.getOffense()))
                        .placeholder("New Offense")))
                .addComponent(ActionRow.of(TextInput.small("carddefence", "Defence", 1, 5)
                        .required(true)
                        .prefilled(String.valueOf(card.getDefence()))
                        .placeholder("New Defence")))
                .build());
    }

    /**
     * Handles the button about editing the cost of the card. The id should
     * be in the format "editcard|[card id]|{@value EditCardUI#ID_COST}"
     */
    private Mono<Void> editCost(ButtonInteractionEvent event, Card card) {
        return event.presentModal(InteractionPresentModalSpec.builder()
                .title("Edit " + card.getName() + "'s Cost")
                .customId(editCardUI.makeId(card, EditCardUI.ID_COST))
                .addComponent(ActionRow.of(TextInput.paragraph("cardcost", "Cost [EWAFNMTD*.]", 0, 12)
                        .required(true)
                        .prefilled(card.getCost())
                        .placeholder("New Cost\n\nMust be a string containing only [EWAFNMTD*.]")))
                .build());
    }

    /**
     * Handles the button about editing the art url of the card. The id should
     * be in the format "editcard|[card id]|{@value EditCardUI#ID_ART_URL}"
     */
    private Mono<Void> editArtUrl(ButtonInteractionEvent event, Card card) {
        return event.presentModal(InteractionPresentModalSpec.builder()
                .title("Edit " + card.getName() + "'s Artwork")
                .customId(editCardUI.makeId(card, EditCardUI.ID_ART_URL))
                .addComponent(ActionRow.of(TextInput.paragraph("cardarturl", "Url to Artwork")
                        .required(true)
                        .prefilled(card.getArtUrl())
                        .placeholder("New Url to the Artwork\n\nWhether the link is working or not is not verified.")))
                .build());
    }

    /**
     * Handles the button about editing the rarity of the card. The id should
     * be in the format "editcard|[card id]|{@value EditCardUI#ID_RARITY}"
     */
    private Mono<Void> editRarity(ButtonInteractionEvent event, Card card) {
        return event.presentModal(InteractionPresentModalSpec.builder()
                .title("Edit " + card.getName() + "'s Rarity")
                .customId(editCardUI.makeId(card, EditCardUI.ID_RARITY))
                .addComponent(ActionRow.of(TextInput.paragraph("cardrarity", "Rarity")
                        .required(true)
                        .prefilled(card.getRarity().toString())
                        .placeholder("New Rarity\n\nMust be one of Basic, Common, Rare, Epic, Legendary or Mythic.")))
                .build());
    }
}
