package net.stelitop.battledudestcg.discord.interactions.buttons;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.battledudestcg.discord.framework.DiscordEventsComponent;
import net.stelitop.battledudestcg.discord.framework.InteractionEvent;
import net.stelitop.battledudestcg.discord.framework.components.ComponentInteraction;
import net.stelitop.battledudestcg.discord.ui.CardCollectionUI;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

@DiscordEventsComponent
public class CardCollectionPageButtonListener {

    @Autowired
    private CardCollectionUI cardCollectionUI;

    @ComponentInteraction(event = ButtonInteractionEvent.class, regex = CardCollectionUI.Model.REGEX)
    public Mono<Void> updateCollectionUI(@InteractionEvent ComponentInteractionEvent event) {
        String buttonId = event.getCustomId();
        var model = CardCollectionUI.Model.deserialize(buttonId);
        if (model.getUserId() != event.getInteraction().getUser().getId().asLong()) {
            return event.reply("This is not your collection message!")
                    .withEphemeral(true);
        }
        MessageCreateSpec message = cardCollectionUI.getCardCollectionMessage(model, event.getInteraction().getUser());
        return event.edit()
                .withContent(message.content())
                .withEmbeds(message.embeds())
                .withComponents(message.components());
    }
}
