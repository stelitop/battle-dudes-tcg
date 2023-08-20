package net.stelitop.battledudestcg.discord.listeners.selectmenus;

import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import net.stelitop.battledudestcg.discord.framework.components.ComponentInteraction;
import net.stelitop.battledudestcg.discord.framework.definition.DiscordEventsComponent;
import net.stelitop.battledudestcg.discord.framework.definition.InteractionEvent;
import net.stelitop.battledudestcg.discord.ui.CardCollectionUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

@DiscordEventsComponent
public class CardCollectionChangeOrderingSelectMenu {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CardCollectionUI cardCollectionUI;

    @ComponentInteraction(
            event = SelectMenuInteractionEvent.class,
            regex = CardCollectionUI.ID_CHANGE_COLLECTION_ORDERING + "\\|" + CardCollectionUI.Model.REGEX
    ) public Mono<Void> handle(@InteractionEvent SelectMenuInteractionEvent event) {
        String eventId = event.getCustomId();
        final String idPrefix = CardCollectionUI.ID_CHANGE_COLLECTION_ORDERING + "|";
        if (event.getValues().size() != 1) return Mono.empty();
        String originalMessageInfoId = eventId.substring(idPrefix.length());
        var model = CardCollectionUI.Model.deserialize(originalMessageInfoId);
        if (model.getUserId() != event.getInteraction().getUser().getId().asLong()) {
            return event.reply("This is not your message!")
                    .withEphemeral(true);
        }
        String newOrdering = event.getValues().get(0);
        model.setOrdering(newOrdering);
        model.setPage(1);
        if (model.validateState().hasFailed()) {
            LOGGER.error("The ordering \"" + newOrdering + "\" was not correctly recognised!");
            return event.reply("The ordering could not be parsed!")
                    .withEphemeral(true);
        }

        var newUI = cardCollectionUI.getCardCollectionMessage(model, event.getInteraction().getUser());
        return event.edit()
                .withContent(newUI.content())
                .withEmbeds(newUI.embeds())
                .withComponents(newUI.components());
    }
}
