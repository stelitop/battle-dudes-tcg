package net.stelitop.battledudestcg.discord.listeners.modals;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import net.stelitop.battledudestcg.commons.pojos.ActionResult;
import net.stelitop.battledudestcg.discord.ui.EditCardUI;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.cards.DudeCard;
import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
import net.stelitop.battledudestcg.game.enums.ElementalType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class EditCardModals implements ApplicationRunner {

    @Autowired
    private GatewayDiscordClient client;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private EditCardUI editCardUI;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        client.on(ModalSubmitInteractionEvent.class, this::handle).subscribe();
    }

    private Mono<Void> handle(ModalSubmitInteractionEvent event) {
        String modalId = event.getCustomId();
        if (!modalId.startsWith("editcard")) return Mono.empty();
        String[] parts = modalId.split("\\|");
        long cardId = Long.parseLong(parts[1]);
        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isEmpty()) {
            return event.reply("An error occurred. Could not find a card with cardId = " + cardId + ".")
                    .withEphemeral(true);
        }
        Card card = cardOpt.get();
        ActionResult<Void> result = switch (parts[2]) { // find which modal this is
            case EditCardUI.ID_NAME -> parseNameModal(event, card);
            case EditCardUI.ID_EFFECT -> parseEffectModal(event, card);
            case EditCardUI.ID_ELEMENTAL_TYPES -> parseElementalTypesModal(event, card);
            case EditCardUI.ID_STATS -> parseStatsModal(event, card);
            case EditCardUI.ID_COST ->  parseCostModal(event, card);
            default -> ActionResult.fail("Could not parse the interaction type.");
        };
        if (result.hasFailed()) {
            return event.reply(result.errorMessage())
                    .withEphemeral(true);
        }

        Card updatedCard = cardRepository.save(card);
        var message = editCardUI.getEditCardMessage(updatedCard);
        return event.edit()
                .withContent(message.content())
                .withEmbeds(message.embeds())
                .withComponents(message.components());
    }

    private ActionResult<Void> parseNameModal(ModalSubmitInteractionEvent event, Card card) {
        String newName = event.getComponents().get(0).getData().components().get().get(0).value().get();
        card.setName(newName);
        return ActionResult.success();
    }

    private ActionResult<Void> parseEffectModal(ModalSubmitInteractionEvent event, Card card) {
        String newEffect = event.getComponents().get(0).getData().components().get().get(0).value().get();
        card.setEffectText(newEffect);
        return ActionResult.success();
    }

    private ActionResult<Void> parseElementalTypesModal(ModalSubmitInteractionEvent event, Card card) {
        String newTypesStr = event.getComponents().get(0).getData().components().get().get(0).value().get();
        var newTypes = ElementalType.parseString(newTypesStr);
        if (newTypes == null) {
            return ActionResult.fail("Could not parse the input \"" + newTypesStr + "\"!");
        }
        newTypes = newTypes.stream().distinct().toList();
        card.setTypes(newTypes);
        return ActionResult.success();
    }

    private ActionResult<Void> parseStatsModal(ModalSubmitInteractionEvent event, Card card) {
        if (!(card instanceof DudeCard d)) return ActionResult.fail("The card was not a dude!");
        String healthStr = event.getComponents().get(0).getData().components().get().get(0).value().get();
        String offenseStr = event.getComponents().get(1).getData().components().get().get(0).value().get();
        String defenceStr = event.getComponents().get(2).getData().components().get().get(0).value().get();
        try {
            int health = Integer.parseInt(healthStr);
            int offense = Integer.parseInt(offenseStr);
            int defence = Integer.parseInt(defenceStr);
            if (health < 0 || offense < 0 || defence < 0) {
                return ActionResult.fail("The stats of the dude must be positive!");
            }
            d.setHealth(health);
            d.setOffense(offense);
            d.setDefence(defence);
            return ActionResult.success();
        } catch (NumberFormatException e) {
            return ActionResult.fail("The input was not a number!");
        }
    }

    private ActionResult<Void> parseCostModal(ModalSubmitInteractionEvent event, Card card) {
        String newCost = event.getComponents().get(0).getData().components().get().get(0).value().get();
        if (ElementalType.parseString(newCost) == null) {
            return ActionResult.fail("Could not parse the input \"" + newCost + "\"!");
        }
        card.setCost(newCost);
        return ActionResult.success();
    }
}
