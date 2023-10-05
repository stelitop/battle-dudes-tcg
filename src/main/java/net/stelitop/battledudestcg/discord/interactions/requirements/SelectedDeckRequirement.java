package net.stelitop.battledudestcg.discord.interactions.requirements;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import net.stelitop.battledudestcg.game.services.DeckService;
import net.stelitop.mad4j.requirements.CommandRequirementExecutor;
import net.stelitop.mad4j.utils.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Requirement for commands that can only be used when the user has selected
 * a deck to edit. For example, if the player wants to add a card to a deck,
 * a deck must have been selected.
 */
@Component
public class SelectedDeckRequirement implements CommandRequirementExecutor {

    @Autowired
    private DeckService deckService;

    @Override
    public ActionResult<Void> verify(ChatInputInteractionEvent event) {
        long userId = event.getInteraction().getUser().getId().asLong();
        var selectedDeckResult = deckService.getSelectedDeck(userId);
        if (selectedDeckResult.hasFailed()) {
            return ActionResult.fail("You must first select a deck before using this command!");
        }
        return ActionResult.success();
    }
}
