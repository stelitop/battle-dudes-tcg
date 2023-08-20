package net.stelitop.battledudestcg.discord.listeners.buttons;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.battledudestcg.discord.framework.components.ComponentInteraction;
import net.stelitop.battledudestcg.discord.framework.DiscordEventsComponent;
import net.stelitop.battledudestcg.discord.framework.InteractionEvent;
import net.stelitop.battledudestcg.discord.ui.ChestInfoUI;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

@DiscordEventsComponent
public class ChestInfoPageButtonListener {

    @Autowired
    private ChestInfoUI chestInfoUI;

    @ComponentInteraction(event = ButtonInteractionEvent.class, regex = ChestInfoUI.Model.REGEX)
    public Mono<Void> updateChestInfoUI(@InteractionEvent ButtonInteractionEvent event) {
        String buttonId = event.getCustomId();
        var model = ChestInfoUI.Model.deserialize(buttonId);
        if (model.getUserId() != event.getInteraction().getUser().getId().asLong()) {
            return event.reply("This is not your collection message!")
                    .withEphemeral(true);
        }
        MessageCreateSpec message = chestInfoUI.getChestInfoMessage(model);
        return event.edit()
                .withContent(message.content())
                .withEmbeds(message.embeds())
                .withComponents(message.components());
    }
}
