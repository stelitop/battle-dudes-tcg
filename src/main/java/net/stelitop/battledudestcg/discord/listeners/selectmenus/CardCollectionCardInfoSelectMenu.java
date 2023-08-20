package net.stelitop.battledudestcg.discord.listeners.selectmenus;


import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.battledudestcg.discord.framework.components.ComponentInteraction;
import net.stelitop.battledudestcg.discord.framework.DiscordEventsComponent;
import net.stelitop.battledudestcg.discord.ui.CardCollectionUI;
import net.stelitop.battledudestcg.discord.ui.CardInfoUI;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.Optional;

@DiscordEventsComponent
public class CardCollectionCardInfoSelectMenu {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardInfoUI cardInfoUI;

    @ComponentInteraction(event = SelectMenuInteractionEvent.class, regex = "opencardinfopage\\|" + CardCollectionUI.Model.REGEX)
    public Mono<Void> openCardInfoPage(SelectMenuInteractionEvent event) {
        String eventId = event.getCustomId();
        final String idPrefix = "opencardinfopage|";
        if (event.getValues().size() != 1) return Mono.empty();
        String originalMessageInfoId = eventId.substring(idPrefix.length());
        var originalModel = CardCollectionUI.Model.deserialize(originalMessageInfoId);
        if (originalModel.getUserId() != event.getInteraction().getUser().getId().asLong()) {
            return event.reply("This is not your message!")
                    .withEphemeral(true);
        }
        String value = event.getValues().get(0);
        Optional<Card> cardOpt = cardRepository.findByNameIgnoreCase(value);
        if (cardOpt.isEmpty()) {
            LOGGER.error("The card \"" + value + "\" was not found in the database, even though it originates from a collection.");
            return event.reply("The card could not be found in the database!")
                    .withEphemeral(true);
        }
        ActionRow newComponents = ActionRow.of(Button.primary(originalMessageInfoId, "Back to Collection"));
        MessageCreateSpec messageCreateSpec = cardInfoUI.getCardInfoMessage(cardOpt.get());
        if (!messageCreateSpec.isComponentsPresent()) {
            messageCreateSpec = messageCreateSpec.withComponents(newComponents);
        } else {
            var currentComponents = messageCreateSpec.components().get();
            currentComponents.add(newComponents);
            messageCreateSpec = messageCreateSpec.withComponents(currentComponents);
        }

        return event.edit()
                .withContent(messageCreateSpec.content())
                .withEmbeds(messageCreateSpec.embeds())
                .withComponents(messageCreateSpec.components());
    }
}
