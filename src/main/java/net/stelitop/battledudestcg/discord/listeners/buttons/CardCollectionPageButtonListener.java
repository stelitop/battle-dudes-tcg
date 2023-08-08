package net.stelitop.battledudestcg.discord.listeners.buttons;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.spec.MessageCreateSpec;
import net.stelitop.battledudestcg.discord.ui.CardCollectionUI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CardCollectionPageButtonListener implements ApplicationRunner {

    @Autowired
    private GatewayDiscordClient client;
    @Autowired
    private CardCollectionUI cardCollectionUI;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        client.on(ButtonInteractionEvent.class, this::handle).subscribe();
    }

    private Mono<Void> handle(ButtonInteractionEvent event) {
        String buttonId = event.getCustomId();
        var model = CardCollectionUI.Model.deserialize(buttonId);
        if (model == null) {
            return Mono.empty();
        }
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
