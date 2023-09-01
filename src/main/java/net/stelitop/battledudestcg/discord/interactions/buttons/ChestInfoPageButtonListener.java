package net.stelitop.battledudestcg.discord.interactions.buttons;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.mad4j.components.ComponentInteraction;
import net.stelitop.mad4j.DiscordEventsComponent;
import net.stelitop.mad4j.InteractionEvent;
import net.stelitop.battledudestcg.discord.ui.ChestInfoUI;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

@DiscordEventsComponent
public class ChestInfoPageButtonListener {

    @Autowired
    private ChestInfoUI chestInfoUI;

    @ComponentInteraction(event = ButtonInteractionEvent.class, regex = ChestInfoUI.Model.REGEX)
    public Mono<Void> updateChestInfoUI(@InteractionEvent ComponentInteractionEvent event) {
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
