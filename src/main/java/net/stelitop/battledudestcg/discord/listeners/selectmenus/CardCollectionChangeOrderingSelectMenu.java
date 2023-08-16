package net.stelitop.battledudestcg.discord.listeners.selectmenus;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import net.stelitop.battledudestcg.discord.ui.CardCollectionUI;
import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CardCollectionChangeOrderingSelectMenu implements ApplicationRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GatewayDiscordClient client;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardCollectionUI cardCollectionUI;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        client.on(SelectMenuInteractionEvent.class, this::handle).subscribe();
    }

    private Mono<Void> handle(SelectMenuInteractionEvent event) {
        String eventId = event.getCustomId();
        final String idPrefix = CardCollectionUI.ID_CHANGE_COLLECTION_ORDERING + "|";
        if (!eventId.startsWith(idPrefix)) return Mono.empty();
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
