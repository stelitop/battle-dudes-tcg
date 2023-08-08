package net.stelitop.battledudestcg.discord.listeners.selectionmenus;


import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.possible.Possible;
import net.stelitop.battledudestcg.discord.ui.CardCollectionUI;
import net.stelitop.battledudestcg.discord.ui.CardInfoUI;
import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class CardCollectionCardInfoSelectMenu implements ApplicationRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GatewayDiscordClient client;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardInfoUI cardInfoUI;

    @Override
    public void run(ApplicationArguments args) {
        client.on(SelectMenuInteractionEvent.class, this::handle).subscribe();
    }

    private Mono<Void> handle(SelectMenuInteractionEvent event) {
        String eventId = event.getCustomId();
        final String idPrefix = "opencardinfopage|";
        if (!eventId.startsWith(idPrefix)) return Mono.empty();
        if (event.getValues().size() != 1) return Mono.empty();
        String originalMessageInfoId = eventId.substring(idPrefix.length());
        String value = event.getValues().get(0);
        Optional<Card> cardOpt = cardRepository.findByNameIgnoreCase(value);
        if (cardOpt.isEmpty()) {
            LOGGER.error("The card \"" + value + "\" was not found in the database, even though it originates from a collection.");
            return event.reply("The card could not be found in the database!")
                    .withEphemeral(true);
        }
        ActionRow newComponents = ActionRow.of(Button.primary(originalMessageInfoId, "Back to Collection"));
        MessageCreateSpec messageCreateSpec = cardInfoUI.getCardInfoEmbed(cardOpt.get());
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
