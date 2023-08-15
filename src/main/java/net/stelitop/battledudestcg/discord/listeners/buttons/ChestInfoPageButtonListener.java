package net.stelitop.battledudestcg.discord.listeners.buttons;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.battledudestcg.discord.ui.ChestInfoUI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ChestInfoPageButtonListener implements ApplicationRunner {

    @Autowired
    private GatewayDiscordClient client;
    @Autowired
    private ChestInfoUI chestInfoUI;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        client.on(ButtonInteractionEvent.class, this::handle).subscribe();
    }

    private Mono<Void> handle(ButtonInteractionEvent event) {
        String buttonId = event.getCustomId();
        var model = ChestInfoUI.Model.deserialize(buttonId);
        if (model == null) {
            return Mono.empty();
        }
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
